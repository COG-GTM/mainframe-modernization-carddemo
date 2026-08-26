using CardDemo.Identity;
using CardDemo.Identity.Cics;
using CardDemo.Identity.Cobol;
using CardDemo.Identity.Copybooks;
using Xunit;

namespace CardDemo.Identity.Tests;

public class Cosgn00cTests
{
    private readonly FakeCicsTerminal _terminal = new();
    private readonly FakeProgramControl _programControl = new();
    private readonly InMemoryUserSecurityFile _usrsec = new();

    private Cosgn00c NewProgram() =>
        new(_terminal, _programControl, _usrsec, () => new DateTime(2024, 3, 7, 14, 5, 9));

    private void GivenUser(string id, string pwd, string type) =>
        _usrsec.Add(new SecUserData
        {
            SecUsrId = id,
            SecUsrFname = "FIRST",
            SecUsrLname = "LAST",
            SecUsrPwd = pwd,
            SecUsrType = type
        });

    private static string Errmsg(Cosgn00c program) =>
        program.Cosgn0a.ErrmsgO.TrimEnd(' ');

    // Rule 1
    [Fact]
    public void FirstEntryWithoutCommareaSendsSignonScreenWithCursorOnUserId()
    {
        var program = NewProgram();

        program.MainPara(new TerminalInput(EibCalen: 0, AttentionKey.Enter));

        var sent = Assert.Single(_terminal.SentMaps);
        Assert.Equal(-1, sent.UseridL);
        Assert.Equal(0, _terminal.ReceiveCount);
        Assert.Empty(_usrsec.ReadKeys);
        Assert.Equal(string.Empty, Errmsg(program));
        Assert.Equal("CC00", program.ReturnTransid);
        Assert.Null(program.XctlProgram);
    }

    [Fact]
    public void FirstEntryPopulatesHeaderInfo()
    {
        var program = NewProgram();

        program.MainPara(new TerminalInput(EibCalen: 0, AttentionKey.Enter));

        var sent = Assert.Single(_terminal.SentMaps);
        Assert.Equal(CcdaScreenTitle.CcdaTitle01, sent.Title01O);
        Assert.Equal(CcdaScreenTitle.CcdaTitle02, sent.Title02O);
        Assert.Equal("CC00", sent.TrnnameO);
        Assert.Equal("COSGN00C", sent.PgmnameO);
        Assert.Equal("03/07/24", sent.CurdateO);
        // CURTIMEO is PIC X(9), so the hh:mm:ss value is space padded.
        Assert.Equal("14:05:09 ", sent.CurtimeO);
        Assert.Equal("CICSAPP1", sent.ApplidO);
        Assert.Equal("CICS    ", sent.SysidO);
    }

    // Rule 2 - PF3
    [Fact]
    public void Pf3SendsThankYouPlainTextAndEndsTheSession()
    {
        var program = NewProgram();

        program.MainPara(new TerminalInput(EibCalen: 100, AttentionKey.Pf3));

        var text = Assert.Single(_terminal.SentTexts);
        Assert.Equal(CobolString.Move(CcdaCommonMessages.CcdaMsgThankYou, 80), text);
        Assert.Empty(_terminal.SentMaps);
        Assert.Null(program.ReturnTransid);
        Assert.Null(program.XctlProgram);
        Assert.Empty(_programControl.Transfers);
    }

    // Rule 2 - any other key
    [Fact]
    public void UnknownKeyShowsInvalidKeyMessageAndRedisplaysScreen()
    {
        var program = NewProgram();

        program.MainPara(new TerminalInput(EibCalen: 100, AttentionKey.Other));

        Assert.Single(_terminal.SentMaps);
        Assert.Equal(CcdaCommonMessages.CcdaMsgInvalidKey.TrimEnd(' '), Errmsg(program));
        Assert.Equal(0, _terminal.ReceiveCount);
        Assert.Empty(_usrsec.ReadKeys);
        Assert.Equal("CC00", program.ReturnTransid);
    }

    // Rule 3 - empty user id
    [Theory]
    [InlineData("        ")]
    [InlineData("\0\0\0\0\0\0\0\0")]
    public void EmptyUserIdRequestsUserIdWithoutReadingTheFile(string userid)
    {
        _terminal.TypedUserid = userid;
        _terminal.TypedPasswd = "PASSWORD";
        var program = NewProgram();

        program.MainPara(new TerminalInput(EibCalen: 100, AttentionKey.Enter));

        var sent = Assert.Single(_terminal.SentMaps);
        Assert.Equal("Please enter User ID ...", Errmsg(program));
        Assert.Equal(-1, sent.UseridL);
        Assert.Empty(_usrsec.ReadKeys);
        Assert.Null(program.XctlProgram);
        Assert.Equal("CC00", program.ReturnTransid);
    }

    // Rule 3 - empty password
    [Theory]
    [InlineData("        ")]
    [InlineData("\0\0\0\0\0\0\0\0")]
    public void EmptyPasswordRequestsPasswordWithoutReadingTheFile(string passwd)
    {
        GivenUser("USER0001", "PASSWORD", "U");
        _terminal.TypedUserid = "USER0001";
        _terminal.TypedPasswd = passwd;
        var program = NewProgram();

        program.MainPara(new TerminalInput(EibCalen: 100, AttentionKey.Enter));

        var sent = Assert.Single(_terminal.SentMaps);
        Assert.Equal("Please enter Password ...", Errmsg(program));
        Assert.Equal(-1, sent.PasswdL);
        Assert.NotEqual(-1, sent.UseridL);
        Assert.Empty(_usrsec.ReadKeys);
        Assert.Null(program.XctlProgram);
    }

    // Rule 4 / 9 - the uppercasing MOVEs run even on the error branches
    [Fact]
    public void UserIdIsMovedToCommareaEvenOnTheEmptyPasswordErrorBranch()
    {
        GivenUser("USER0001", "PASSWORD", "U");
        _terminal.TypedUserid = "user0001";
        _terminal.TypedPasswd = "        ";
        var program = NewProgram();

        program.MainPara(new TerminalInput(EibCalen: 100, AttentionKey.Enter));

        Assert.Equal("USER0001", program.CarddemoCommarea.CdemoUserId);
        Assert.Empty(_usrsec.ReadKeys);
    }

    // Rules 6 / 12 - valid admin
    [Fact]
    public void ValidAdminCredentialsPopulateCommareaAndTransferToCoadm01C()
    {
        GivenUser("ADMIN001", "PASSWORD", "A");
        _terminal.TypedUserid = "ADMIN001";
        _terminal.TypedPasswd = "PASSWORD";
        var program = NewProgram();

        program.MainPara(new TerminalInput(EibCalen: 100, AttentionKey.Enter));

        var transfer = Assert.Single(_programControl.Transfers);
        Assert.Equal("COADM01C", transfer.Program);
        Assert.Equal("COADM01C", program.XctlProgram);
        Assert.Empty(_terminal.SentMaps);
        Assert.Null(program.ReturnTransid);

        var commarea = program.CarddemoCommarea;
        Assert.Equal("CC00", commarea.CdemoFromTranid);
        Assert.Equal("COSGN00C", commarea.CdemoFromProgram);
        Assert.Equal("ADMIN001", commarea.CdemoUserId);
        Assert.Equal("A", commarea.CdemoUserType);
        Assert.True(commarea.CdemoUsrtypAdmin);
        Assert.Equal(0, commarea.CdemoPgmContext);
        Assert.Same(commarea, transfer.Commarea);
    }

    // Rule 6 - valid regular user
    [Fact]
    public void ValidRegularUserCredentialsTransferToComen01C()
    {
        GivenUser("USER0001", "PASSWORD", "U");
        _terminal.TypedUserid = "USER0001";
        _terminal.TypedPasswd = "PASSWORD";
        var program = NewProgram();

        program.MainPara(new TerminalInput(EibCalen: 100, AttentionKey.Enter));

        var transfer = Assert.Single(_programControl.Transfers);
        Assert.Equal("COMEN01C", transfer.Program);
        Assert.Equal("U", program.CarddemoCommarea.CdemoUserType);
        Assert.Empty(_terminal.SentMaps);
    }

    // Rule 6 - a non-admin, non-'U' user type still routes to the menu program
    [Fact]
    public void NonAdminUserTypeTransfersToComen01C()
    {
        GivenUser("USER0002", "PASSWORD", "X");
        _terminal.TypedUserid = "USER0002";
        _terminal.TypedPasswd = "PASSWORD";
        var program = NewProgram();

        program.MainPara(new TerminalInput(EibCalen: 100, AttentionKey.Enter));

        Assert.Equal("COMEN01C", Assert.Single(_programControl.Transfers).Program);
    }

    // Rule 6 - wrong password
    [Fact]
    public void WrongPasswordRedisplaysScreenWithCursorOnPassword()
    {
        GivenUser("USER0001", "PASSWORD", "U");
        _terminal.TypedUserid = "USER0001";
        _terminal.TypedPasswd = "BADPWD";
        var program = NewProgram();

        program.MainPara(new TerminalInput(EibCalen: 100, AttentionKey.Enter));

        var sent = Assert.Single(_terminal.SentMaps);
        Assert.Equal("Wrong Password. Try again ...", Errmsg(program));
        Assert.Equal(-1, sent.PasswdL);
        Assert.Empty(_programControl.Transfers);
        Assert.Null(program.XctlProgram);
        Assert.Equal("CC00", program.ReturnTransid);
    }

    // Rule 6 - RESP 13 (NOTFND)
    [Fact]
    public void UnknownUserRedisplaysScreenWithCursorOnUserId()
    {
        _terminal.TypedUserid = "NOSUCH01";
        _terminal.TypedPasswd = "PASSWORD";
        var program = NewProgram();

        program.MainPara(new TerminalInput(EibCalen: 100, AttentionKey.Enter));

        var sent = Assert.Single(_terminal.SentMaps);
        Assert.Equal("User not found. Try again ...", Errmsg(program));
        Assert.Equal(-1, sent.UseridL);
        Assert.Empty(_programControl.Transfers);
    }

    // Rule 6 - any other RESP
    [Fact]
    public void OtherFileErrorRedisplaysScreenWithCursorOnUserId()
    {
        GivenUser("USER0001", "PASSWORD", "U");
        _usrsec.ForcedRespCode = 12;
        _terminal.TypedUserid = "USER0001";
        _terminal.TypedPasswd = "PASSWORD";
        var program = NewProgram();

        program.MainPara(new TerminalInput(EibCalen: 100, AttentionKey.Enter));

        var sent = Assert.Single(_terminal.SentMaps);
        Assert.Equal("Unable to verify the User ...", Errmsg(program));
        Assert.Equal(-1, sent.UseridL);
        Assert.Empty(_programControl.Transfers);
    }

    // Rule 11 - uppercasing before lookup and comparison
    [Fact]
    public void LowercaseCredentialsAreUppercasedBeforeLookupAndCompare()
    {
        GivenUser("ADMIN001", "PASSWORD", "A");
        _terminal.TypedUserid = "admin001";
        _terminal.TypedPasswd = "password";
        var program = NewProgram();

        program.MainPara(new TerminalInput(EibCalen: 100, AttentionKey.Enter));

        Assert.Equal("ADMIN001", Assert.Single(_usrsec.ReadKeys));
        Assert.Equal("COADM01C", Assert.Single(_programControl.Transfers).Program);
        Assert.Equal("ADMIN001", program.CarddemoCommarea.CdemoUserId);
    }

    // Rule 6 - the read is issued against the USRSEC dataset only
    [Fact]
    public void ReadIsIssuedAgainstUsrsecDataset()
    {
        GivenUser("USER0001", "PASSWORD", "U");
        _terminal.TypedUserid = "USER0001";
        _terminal.TypedPasswd = "PASSWORD";
        var program = NewProgram();

        program.MainPara(new TerminalInput(EibCalen: 100, AttentionKey.Enter));

        Assert.Equal("USRSEC  ", Assert.Single(_usrsec.ReadDatasets));
    }

    // Rule 7 - pseudo-conversational loop: each redisplay returns with CC00
    [Fact]
    public void FailedSignonThenSuccessfulSignonAcrossTwoInteractions()
    {
        GivenUser("USER0001", "PASSWORD", "U");

        _terminal.TypedUserid = "USER0001";
        _terminal.TypedPasswd = "BADPWD";
        var first = NewProgram();
        first.MainPara(new TerminalInput(EibCalen: 100, AttentionKey.Enter));
        Assert.Equal("CC00", first.ReturnTransid);

        _terminal.TypedPasswd = "PASSWORD";
        var second = NewProgram();
        second.MainPara(new TerminalInput(EibCalen: 100, AttentionKey.Enter));

        Assert.Equal("COMEN01C", second.XctlProgram);
        Assert.Null(second.ReturnTransid);
    }

    // Rule 8 - read-only against USRSEC: the stored record is never modified.
    [Fact]
    public void SignonDoesNotModifyTheStoredUserRecord()
    {
        GivenUser("USER0001", "PASSWORD", "U");
        _terminal.TypedUserid = "USER0001";
        _terminal.TypedPasswd = "PASSWORD";
        var program = NewProgram();

        program.MainPara(new TerminalInput(EibCalen: 100, AttentionKey.Enter));

        var record = new SecUserData();
        Assert.Equal(0, _usrsec.Read("USRSEC  ", "USER0001", record));
        Assert.Equal("PASSWORD", record.SecUsrPwd);
        Assert.Equal("U", record.SecUsrType);
    }
}
