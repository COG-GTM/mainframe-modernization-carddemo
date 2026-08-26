using CardDemo.Identity.Bms;
using CardDemo.Identity.Cics;
using CardDemo.Identity.Cobol;
using CardDemo.Identity.Copybooks;

namespace CardDemo.Identity;

/// <summary>
/// COSGN00C.CBL - CICS COBOL signon program for transaction CC00, ported
/// one-to-one. Each COBOL paragraph is a method of the same name and the
/// statement order inside each paragraph is preserved.
/// </summary>
public sealed class Cosgn00c
{
    // 01 WS-VARIABLES.
    private const string WsPgmname = "COSGN00C";                 // PIC X(08)
    private const string WsTranid = "CC00";                      // PIC X(04)
    private const string WsUsrsecFile = "USRSEC  ";              // PIC X(08)
    private string _wsMessage = CobolString.Spaces(80);          // PIC X(80)
    private string _wsErrFlg = "N";                              // PIC X(01)
    private int WsRespCd { get; set; }                            // PIC S9(09) COMP
    private int WsReasCd { get; set; }                            // PIC S9(09) COMP
    private string _wsUserId = CobolString.Spaces(8);            // PIC X(08)
    private string _wsUserPwd = CobolString.Spaces(8);           // PIC X(08)

    // 88 ERR-FLG-ON VALUE 'Y'. / 88 ERR-FLG-OFF VALUE 'N'.
    private bool ErrFlgOn => _wsErrFlg == "Y";

    // COPY CSUSR01Y - the USRSEC record read into working storage.
    private readonly SecUserData _secUserData = new();

    private readonly ICicsTerminal _terminal;
    private readonly IProgramControl _programControl;
    private readonly IUserSecurityFile _userSecurityFile;
    private readonly Func<DateTime> _currentDate;

    private TerminalInput _eib = new(0, AttentionKey.Enter);
    private bool _taskEnded;

    public Cosgn00c(
        ICicsTerminal terminal,
        IProgramControl programControl,
        IUserSecurityFile userSecurityFile,
        Func<DateTime>? currentDate = null)
    {
        _terminal = terminal;
        _programControl = programControl;
        _userSecurityFile = userSecurityFile;
        _currentDate = currentDate ?? (() => DateTime.Now);
    }

    // COPY COCOM01Y - CARDDEMO-COMMAREA in working storage.
    public CardDemoCommarea CarddemoCommarea { get; } = new();

    // COPY COSGN00 - symbolic map storage COSGN0AI / COSGN0AO.
    public Cosgn0aMap Cosgn0a { get; } = new();

    /// <summary>
    /// TRANSID of the EXEC CICS RETURN that ended the task, or null when the
    /// task ended without a next transaction (the PF3 path) or transferred
    /// control with XCTL.
    /// </summary>
    public string? ReturnTransid { get; private set; }

    /// <summary>Program named on the EXEC CICS XCTL, or null when no transfer happened.</summary>
    public string? XctlProgram { get; private set; }

    //----------------------------------------------------------------*
    //                      MAIN-PARA
    //----------------------------------------------------------------*
    public void MainPara(TerminalInput eib)
    {
        _eib = eib;

        // SET ERR-FLG-OFF TO TRUE
        _wsErrFlg = "N";

        // MOVE SPACES TO WS-MESSAGE ERRMSGO OF COSGN0AO
        _wsMessage = CobolString.Spaces(80);
        Cosgn0a.ErrmsgO = CobolString.Spaces(78);

        if (_eib.EibCalen == 0)
        {
            Cosgn0a.MoveLowValuesToOutput();
            Cosgn0a.UseridL = -1;
            SendSignonScreen();
        }
        else
        {
            switch (_eib.EibAid)
            {
                case AttentionKey.Enter:
                    ProcessEnterKey();
                    break;
                case AttentionKey.Pf3:
                    _wsMessage = CobolString.Move(CcdaCommonMessages.CcdaMsgThankYou, 80);
                    SendPlainText();
                    break;
                default:
                    _wsErrFlg = "Y";
                    _wsMessage = CobolString.Move(CcdaCommonMessages.CcdaMsgInvalidKey, 80);
                    SendSignonScreen();
                    break;
            }
        }

        // EXEC CICS RETURN TRANSID(WS-TRANID) COMMAREA(CARDDEMO-COMMAREA).
        // Not reached when an earlier paragraph already ended the task with a
        // bare RETURN (SEND-PLAIN-TEXT) or transferred control with XCTL.
        if (_taskEnded)
        {
            return;
        }

        ReturnTransid = WsTranid;
        _taskEnded = true;
    }

    //----------------------------------------------------------------*
    //                      PROCESS-ENTER-KEY
    //----------------------------------------------------------------*
    private void ProcessEnterKey()
    {
        // EXEC CICS RECEIVE MAP('COSGN0A') MAPSET('COSGN00')
        WsRespCd = _terminal.ReceiveMap("COSGN0A", "COSGN00", Cosgn0a);
        WsReasCd = 0;

        if (CobolString.IsSpacesOrLowValues(Cosgn0a.UseridI))
        {
            _wsErrFlg = "Y";
            _wsMessage = CobolString.Move("Please enter User ID ...", 80);
            Cosgn0a.UseridL = -1;
            SendSignonScreen();
        }
        else if (CobolString.IsSpacesOrLowValues(Cosgn0a.PasswdI))
        {
            _wsErrFlg = "Y";
            _wsMessage = CobolString.Move("Please enter Password ...", 80);
            Cosgn0a.PasswdL = -1;
            SendSignonScreen();
        }

        // The uppercasing MOVEs follow the EVALUATE, so they also run on the
        // error branches above - preserved as in the COBOL.
        _wsUserId = CobolString.Move(CobolString.UpperCase(Cosgn0a.UseridI), 8);
        CarddemoCommarea.CdemoUserId = CobolString.Move(CobolString.UpperCase(Cosgn0a.UseridI), 8);
        _wsUserPwd = CobolString.Move(CobolString.UpperCase(Cosgn0a.PasswdI), 8);

        if (!ErrFlgOn)
        {
            ReadUserSecFile();
        }
    }

    //----------------------------------------------------------------*
    //                      SEND-SIGNON-SCREEN
    //----------------------------------------------------------------*
    private void SendSignonScreen()
    {
        PopulateHeaderInfo();

        // MOVE WS-MESSAGE TO ERRMSGO OF COSGN0AO
        Cosgn0a.ErrmsgO = CobolString.Move(_wsMessage, 78);

        _terminal.SendMap("COSGN0A", "COSGN00", Cosgn0a);
    }

    //----------------------------------------------------------------*
    //                      SEND-PLAIN-TEXT
    //----------------------------------------------------------------*
    private void SendPlainText()
    {
        _terminal.SendText(_wsMessage, _wsMessage.Length);

        // EXEC CICS RETURN - ends the task without a next transaction.
        _taskEnded = true;
    }

    //----------------------------------------------------------------*
    //                      POPULATE-HEADER-INFO
    //----------------------------------------------------------------*
    private void PopulateHeaderInfo()
    {
        // MOVE FUNCTION CURRENT-DATE TO WS-CURDATE-DATA
        var now = _currentDate();

        Cosgn0a.Title01O = CobolString.Move(CcdaScreenTitle.CcdaTitle01, 40);
        Cosgn0a.Title02O = CobolString.Move(CcdaScreenTitle.CcdaTitle02, 40);
        Cosgn0a.TrnnameO = CobolString.Move(WsTranid, 4);
        Cosgn0a.PgmnameO = CobolString.Move(WsPgmname, 8);

        // WS-CURDATE-MM-DD-YY (mm/dd/yy) from CSDAT01Y.
        Cosgn0a.CurdateO = CobolString.Move(
            $"{now.Month:D2}/{now.Day:D2}/{now.Year % 100:D2}", 8);

        // WS-CURTIME-HH-MM-SS (hh:mm:ss) from CSDAT01Y.
        Cosgn0a.CurtimeO = CobolString.Move(
            $"{now.Hour:D2}:{now.Minute:D2}:{now.Second:D2}", 9);

        Cosgn0a.ApplidO = CobolString.Move(_terminal.AssignApplid(), 8);
        Cosgn0a.SysidO = CobolString.Move(_terminal.AssignSysid(), 8);
    }

    //----------------------------------------------------------------*
    //                      READ-USER-SEC-FILE
    //----------------------------------------------------------------*
    private void ReadUserSecFile()
    {
        // EXEC CICS READ DATASET(WS-USRSEC-FILE) INTO(SEC-USER-DATA)
        //               RIDFLD(WS-USER-ID)
        WsRespCd = _userSecurityFile.Read(WsUsrsecFile, _wsUserId, _secUserData);
        WsReasCd = 0;

        switch (WsRespCd)
        {
            case CicsRespCodes.Normal:
                if (_secUserData.SecUsrPwd == _wsUserPwd)
                {
                    CarddemoCommarea.CdemoFromTranid = CobolString.Move(WsTranid, 4);
                    CarddemoCommarea.CdemoFromProgram = CobolString.Move(WsPgmname, 8);
                    CarddemoCommarea.CdemoUserId = CobolString.Move(_wsUserId, 8);
                    CarddemoCommarea.CdemoUserType = CobolString.Move(_secUserData.SecUsrType, 1);
                    CarddemoCommarea.CdemoPgmContext = 0;

                    if (CarddemoCommarea.CdemoUsrtypAdmin)
                    {
                        Xctl("COADM01C");
                    }
                    else
                    {
                        Xctl("COMEN01C");
                    }
                }
                else
                {
                    _wsMessage = CobolString.Move("Wrong Password. Try again ...", 80);
                    Cosgn0a.PasswdL = -1;
                    SendSignonScreen();
                }

                break;

            case CicsRespCodes.Notfnd:
                _wsErrFlg = "Y";
                _wsMessage = CobolString.Move("User not found. Try again ...", 80);
                Cosgn0a.UseridL = -1;
                SendSignonScreen();
                break;

            default:
                _wsErrFlg = "Y";
                _wsMessage = CobolString.Move("Unable to verify the User ...", 80);
                Cosgn0a.UseridL = -1;
                SendSignonScreen();
                break;
        }
    }

    // EXEC CICS XCTL PROGRAM(...) COMMAREA(CARDDEMO-COMMAREA) - control does
    // not come back to this program, so the task ends here.
    private void Xctl(string program)
    {
        XctlProgram = program;
        _programControl.Xctl(program, CarddemoCommarea);
        _taskEnded = true;
    }
}
