# ts-app fixtures

`usrsec.txt` is the USRSEC (user security) file in ASCII, LRECL 80, laid out per
`app/cpy/CSUSR01Y.cpy`. The COBOL repository only ships this file as EBCDIC
(`app/data/EBCDIC/AWS.M2.CARDDEMO.USRSEC.PS`); the records here are the in-stream
`SYSUT1` data of `app/jcl/DUSRSECJ.jcl`, which is what that dataset is built from.

Regenerate with:

```
awk '/^ADMIN|^USER/{printf "%-80s\n", $0}' app/jcl/DUSRSECJ.jcl > ts-app/data/usrsec.txt
```

Every other file the TypeScript app reads comes from `app/data/ASCII/` unchanged.
