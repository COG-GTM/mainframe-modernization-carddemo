using CardDemo.Identity.Cobol;

namespace CardDemo.Identity.Bms;

/// <summary>
/// COSGN00.CPY - symbolic map storage for map COSGN0A of mapset COSGN00.
/// COSGN0AO REDEFINES COSGN0AI, so a single object carries the input (xxxxI),
/// output (xxxxO), length/cursor (xxxxL) and attribute (xxxxA) fields.
/// </summary>
public sealed class Cosgn0aMap
{
    // ---- COSGN0AI (input) ----
    public short TrnnameL { get; set; }
    public string TrnnameI { get; set; } = CobolString.Spaces(4);   // PIC X(4)
    public short Title01L { get; set; }
    public string Title01I { get; set; } = CobolString.Spaces(40);  // PIC X(40)
    public short CurdateL { get; set; }
    public string CurdateI { get; set; } = CobolString.Spaces(8);   // PIC X(8)
    public short PgmnameL { get; set; }
    public string PgmnameI { get; set; } = CobolString.Spaces(8);   // PIC X(8)
    public short Title02L { get; set; }
    public string Title02I { get; set; } = CobolString.Spaces(40);  // PIC X(40)
    public short CurtimeL { get; set; }
    public string CurtimeI { get; set; } = CobolString.Spaces(9);   // PIC X(9)
    public short ApplidL { get; set; }
    public string ApplidI { get; set; } = CobolString.Spaces(8);    // PIC X(8)
    public short SysidL { get; set; }
    public string SysidI { get; set; } = CobolString.Spaces(8);     // PIC X(8)
    public short UseridL { get; set; }
    public string UseridI { get; set; } = CobolString.Spaces(8);    // PIC X(8)
    public short PasswdL { get; set; }
    public string PasswdI { get; set; } = CobolString.Spaces(8);    // PIC X(8)
    public short ErrmsgL { get; set; }
    public string ErrmsgI { get; set; } = CobolString.Spaces(78);   // PIC X(78)

    // ---- COSGN0AO (output) ----
    public string TrnnameO { get; set; } = CobolString.Spaces(4);   // PIC X(4)
    public string Title01O { get; set; } = CobolString.Spaces(40);  // PIC X(40)
    public string CurdateO { get; set; } = CobolString.Spaces(8);   // PIC X(8)
    public string PgmnameO { get; set; } = CobolString.Spaces(8);   // PIC X(8)
    public string Title02O { get; set; } = CobolString.Spaces(40);  // PIC X(40)
    public string CurtimeO { get; set; } = CobolString.Spaces(9);   // PIC X(9)
    public string ApplidO { get; set; } = CobolString.Spaces(8);    // PIC X(8)
    public string SysidO { get; set; } = CobolString.Spaces(8);     // PIC X(8)
    public string UseridO { get; set; } = CobolString.Spaces(8);    // PIC X(8)
    public string PasswdO { get; set; } = CobolString.Spaces(8);    // PIC X(8)
    public string ErrmsgO { get; set; } = CobolString.Spaces(78);   // PIC X(78)

    /// <summary>MOVE LOW-VALUES TO COSGN0AO (clears the redefined map storage).</summary>
    public void MoveLowValuesToOutput()
    {
        TrnnameO = CobolString.LowValues(4);
        Title01O = CobolString.LowValues(40);
        CurdateO = CobolString.LowValues(8);
        PgmnameO = CobolString.LowValues(8);
        Title02O = CobolString.LowValues(40);
        CurtimeO = CobolString.LowValues(9);
        ApplidO = CobolString.LowValues(8);
        SysidO = CobolString.LowValues(8);
        UseridO = CobolString.LowValues(8);
        PasswdO = CobolString.LowValues(8);
        ErrmsgO = CobolString.LowValues(78);

        TrnnameI = CobolString.LowValues(4);
        Title01I = CobolString.LowValues(40);
        CurdateI = CobolString.LowValues(8);
        PgmnameI = CobolString.LowValues(8);
        Title02I = CobolString.LowValues(40);
        CurtimeI = CobolString.LowValues(9);
        ApplidI = CobolString.LowValues(8);
        SysidI = CobolString.LowValues(8);
        UseridI = CobolString.LowValues(8);
        PasswdI = CobolString.LowValues(8);
        ErrmsgI = CobolString.LowValues(78);

        TrnnameL = 0;
        Title01L = 0;
        CurdateL = 0;
        PgmnameL = 0;
        Title02L = 0;
        CurtimeL = 0;
        ApplidL = 0;
        SysidL = 0;
        UseridL = 0;
        PasswdL = 0;
        ErrmsgL = 0;
    }

    /// <summary>Snapshot of the map, used by terminals to record what was sent.</summary>
    public Cosgn0aMap Clone() => (Cosgn0aMap)MemberwiseClone();
}
