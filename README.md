## ECIRetail -- Mainframe Retail Application (El Corte Ingles)

- [ECIRetail -- Mainframe Retail Application](#eciretail----mainframe-retail-application-el-corte-ingles)
- [Description](#description)
- [Technologies used](#technologies-used)
- [Installation on the mainframe](#installation-on-the-mainframe)
- [Application Details](#application-details)
  - [User Functions](#user-functions)
  - [Admin Functions](#admin-functions)
  - [Application Inventory](#application-inventory)
    - [**Online**](#online)
    - [**Batch**](#batch)
  - [Batch Processing Flow](#batch-processing-flow)
- [Support](#support)
- [License](#license)

<br/>

## Description
ECIRetail is a Mainframe application that simulates the retail point-of-sale and back-office operations for **El Corte Ingles (ECI)**, Spain's largest department store chain operating **123 retail centres** nationwide.

The application manages:
- **Centro/Tienda** (store) master data for all 123 locations
- **Cliente** (customer) CRM with segmentation (Premium, Estandar, Joven)
- **Tarjeta de Fidelizacion** (loyalty card) lifecycle
- **Ticket de Venta** (sales transactions) with centro partitioning
- **Batch consolidation** of daily sales across all 123 centres
- **Loyalty points and discount** calculation per customer segment
- **Reporting** (daily sales statements, consolidated reports)

This application serves as a mainframe modernization demonstration, providing realistic retail legacy scenarios for discovery, migration, performance testing, service extraction, and test creation.

Note: the coding style intentionally varies across the application to exercise analysis and transformation tooling.

<br/>

## Technologies used
1. COBOL
2. CICS
3. VSAM (KSDS)
4. JCL
5. BMS (3270 screens)
6. RACF

<br/>

## Installation on the mainframe

To install this repository on the mainframe:

1. Clone this repository to your local development environment

2. Create datasets on the mainframe
   * Use HLQ `MFE.ECIRETAIL` (or your preferred qualifier)
   * Upload application source folders using $INDFILE or your preferred tool

3. Load sample data

   | Dataset name                       | Description                             | Copybook   | Format | Length | ASCII file       |
   | :--------------------------------- | :-------------------------------------- | :--------- | :----- | -----: | :--------------- |
   | MFE.ECIRETAIL.USRSEC.PS           | User Security file                      | EISEC01Y   | FB     |     80 | See DUSRSECJ.jcl |
   | MFE.ECIRETAIL.CENTROS.PS          | Centro/Tienda master (123 centros)      | EICNT01Y   | FB     |    300 | centros.txt      |
   | MFE.ECIRETAIL.CLIENTES.PS         | Cliente ECI data                        | EICUS01Y   | FB     |    500 | clientes.txt     |
   | MFE.ECIRETAIL.TARJETAS.PS         | Tarjeta Fidelizacion data               | EITJF01Y   | FB     |    150 | tarjetas.txt     |
   | MFE.ECIRETAIL.TARJXREF.PS         | Tarjeta-Cliente-Centro cross reference  | EIXRF03Y   | FB     |     50 | tarjxref.txt     |
   | MFE.ECIRETAIL.TICKETS.PS.INIT     | Ticket initialization record            | EITKT06Y   | FB     |    350 | (1 init record)  |
   | MFE.ECIRETAIL.DALYTKT.PS          | Daily tickets pending posting           | EITKT06Y   | FB     |    350 | dailytkt.txt     |
   | MFE.ECIRETAIL.VCATBAL.PS          | Venta category balance per centro       | EICAT01Y   | FB     |     50 | vcatbal.txt      |
   | MFE.ECIRETAIL.PRODTYPE.PS         | Product types                           | EICAT03Y   | FB     |     60 | prodtype.txt     |
   | MFE.ECIRETAIL.PRODCATG.PS         | Product categories                      | EICAT04Y   | FB     |     60 | prodcatg.txt     |
   | MFE.ECIRETAIL.DESCTGRP.PS         | Discount/Points policy groups           | EICAT02Y   | FB     |     50 | desctgrp.txt     |

   * Execute the following JCLs in order:

     | Jobname  | What it does                                            |
     | :------- | :------------------------------------------------------ |
     | DUSRSECJ | Sets up user security VSAM file                         |
     | CLOSEFIL | Closes files opened by CICS                             |
     | CNTROFL  | Loads Centro/Tienda master (123 centres)                |
     | TARJFIDL | Loads Tarjeta Fidelizacion database                     |
     | CLIENTFL | Creates Cliente ECI database                            |
     | XREFFILE | Loads Tarjeta-Cliente-Centro cross reference to VSAM    |
     | TICKTFL  | Copies initial Ticket file to VSAM                      |
     | DESCTGRP | Loads Discount/Points policy groups to VSAM             |
     | VCATBALF | Loads initial venta category balance to VSAM            |
     | PRODCATG | Loads product category file to VSAM                     |
     | PRODTYPE | Loads product type file                                 |
     | OPENFIL  | Makes files available to CICS                           |
     | DEFGDGB  | Defines GDG Bases                                       |

4. Compile the Programs
   Use your mainframe compile process. Sample JCLs provided in the samples folder.

5. Create CICS resources in the ECIRETAIL group
   * Use the DFHCSDUP utility with the CSD file provided in `app/csd/`
   * Group ECIRETAIL: Mapsets, Transactions, Programs, Files
   * Install/Load: `CEDA INSTALL GROUP(ECIRETAIL)`

6. Run the application
   * **Online**: Start with transaction `EC00`
     - User `ADMIN001` / `PASSWORD` for administration
     - User `USER0001` / `PASSWORD` for back-office functions
   * **Batch**: See "Running full batch" below

<br/>

## Running full batch

Execute the following JCLs in order for a complete batch cycle across 123 centres:

| Jobname  | What it does                                            |
| :------- | :------------------------------------------------------ |
| CLOSEFIL | Closes files opened by CICS                             |
| CNTROFL  | Loads Centro/Tienda master (123 centres)                |
| TARJFIDL | Loads Tarjeta Fidelizacion database                     |
| XREFFILE | Loads Tarjeta-Cliente-Centro cross reference            |
| CLIENTFL | Creates Cliente ECI database                            |
| TKTBKP   | Backup Ticket database                                  |
| DESCTGRP | Loads Discount/Points policy groups to VSAM             |
| VCATBALF | Refreshes venta category balance                        |
| PRODTYPE | Loads product type file                                 |
| DUSRSECJ | Sets up user security VSAM file                         |
| POSTTKT  | **Core processing**: posts daily tickets to master      |
| FIDCALC  | **Loyalty calc**: computes points & discounts per centro|
| TKTBKP   | Backup Ticket database (post-processing)                |
| COMBTKT  | Combine system transactions with daily ones             |
| CREASTMT | Produce daily sales statement per centro                |
| TRANREPT | Produce consolidated report by centro/category          |
| TKTAIDX  | Define alternate index on ticket file (by centro)       |
| OPENFIL  | Makes files available to CICS                           |

<br/>

## Application Details
ECIRetail is a retail point-of-sale management application for El Corte Ingles, built using COBOL. It manages Centro/Tienda data, customer loyalty programmes, sales tickets, and daily batch consolidation across 123 centres.

There are 2 types of users:
* **Regular User** - back-office functions (view centres, manage loyalty cards, register sales, request reports)
* **Admin User** - user administration functions

<br/>

### User Functions

| Function                    | Description                                    |
| :-------------------------- | :--------------------------------------------- |
| Ver Centro/Tienda           | View store details for any of the 123 centres  |
| Actualizar Centro           | Update store information                       |
| Listar Tarjetas Fidelizacion| Browse loyalty cards                          |
| Ver Tarjeta                 | View loyalty card details                      |
| Listar Tickets              | Browse sales tickets                           |
| Registrar Venta             | Enter a new sales ticket from POS terminal     |
| Informes                    | Request sales reports                          |

<br/>

### Admin Functions

| Function           | Description              |
| :----------------- | :----------------------- |
| Listar Usuarios    | Browse system users      |
| Anadir Usuario     | Create new user          |
| Modificar Usuario  | Update user details      |
| Eliminar Usuario   | Remove user              |

<br/>

### Application Inventory

#### **Online**

| Transaction | Code | BMS Map  | Program  | Function                          |
| :---------- | :--- | :------- | :------- | :-------------------------------- |
| EC00        |      | EISGN00  | EISGN00C | Signon Screen                     |
| EM00        |      | EIMEN01  | EIMEN01C | Main Menu                         |
|             | CV   | EICNTVW  | EICNTVWC | Centro/Tienda View                |
|             | CU   | EICNTUP  | EICNTUPC | Centro/Tienda Update              |
|             | TL   | EITJFLI  | EITJFLIC | Tarjeta Fidelizacion List         |
|             | TV   | EITJFSL  | EITJFSLC | Tarjeta Fidelizacion View         |
|             | VL   | EITKT00  | EITKT00C | Ticket/Venta List                 |
|             | VA   | EITKT02  | EITKT02C | Ticket/Venta Add                  |
|             | RI   | EIRPT00  | EIRPT00C | Reports                           |
| EA00        |      | EIADM01  | EIADM01C | Admin Menu                        |
|             | UL   | EIUSR00  | EIUSR00C | List Users                        |
|             | UA   | EIUSR01  | EIUSR01C | Add User                          |
|             | UM   | EIUSR02  | EIUSR02C | Update User                       |
|             | UE   | EIUSR03  | EIUSR03C | Delete User                       |

#### **Batch**

| Job      | Program  | Function                                              |
| :------- | :------- | :---------------------------------------------------- |
| DUSRSECJ | IEBGENER | Initial Load of User security file                    |
| DEFGDGB  | IDCAMS   | Setup GDG Bases                                       |
| CNTROFL  | IDCAMS   | Refresh Centro/Tienda Master (123 centres)            |
| TARJFIDL | IDCAMS   | Refresh Tarjeta Fidelizacion Master                   |
| CLIENTFL | IDCAMS   | Refresh Cliente ECI Master                            |
| DESCTGRP | IDCAMS   | Load Discount/Points Policy Groups                    |
| TICKTFL  | IDCAMS   | Load Ticket Master file                               |
| PRODCATG | IDCAMS   | Load Product category types                           |
| PRODTYPE | IDCAMS   | Load Product type file                                |
| XREFFILE | IDCAMS   | Tarjeta, Cliente and Centro cross reference           |
| CLOSEFIL | IEFBR14  | Close VSAM files in CICS                              |
| VCATBALF | IDCAMS   | Refresh Venta Category Balance per centro             |
| TKTBKP   | IDCAMS   | Backup Ticket Master                                  |
| POSTTKT  | EITRN02C | **Post daily tickets** (validates centro, updates balance) |
| TKTAIDX  | IDCAMS   | Define AIX for ticket file (by centro)                |
| OPENFIL  | IEFBR14  | Open files in CICS                                    |
| FIDCALC  | EIFID04C | **Loyalty points & discount calculation** per centro  |
| COMBTKT  | SORT     | Combine ticket files                                  |
| CREASTMT | EISTM03A | **Produce daily sales statement** per centro          |
| TRANREPT | EIRPT03C | **Consolidated report** by centro/category            |

<br/>

### Batch Processing Flow

The daily batch cycle processes sales across all 123 ECI centres:

```
1. CLOSEFIL  - Quiesce CICS files
2. CNTROFL   - Refresh centro master (123 stores)
3. POSTTKT   - Post daily tickets:
               * Read DALYTKT (daily POS tickets)
               * Validate centro exists in master
               * Validate tarjeta fidelizacion (if present)
               * Write to TICKETS VSAM master
               * Update VCATBALF (venta category balance per centro)
               * Write rejects to GDG
4. FIDCALC   - Calculate loyalty points:
               * Read VCATBALF sequentially by centro
               * For each centro/category combination:
                 - Look up discount policy (DESCTGRP)
                 - Compute points = sales_amount * points_per_euro
                 - Compute discount = sales_amount * discount_pct
               * Write fidelity transactions to GDG
5. COMBTKT   - Merge fidelity transactions with daily tickets
6. CREASTMT  - Generate daily sales statement per centro
7. TRANREPT  - Consolidated report by centro + category
8. TKTAIDX   - Rebuild alternate index (by centro for queries)
9. OPENFIL   - Reopen files for CICS online
```

<br/>

## Support

If you have questions or requests for improvement please raise an issue in the repository.

<br/>

## License

This is released under the Apache 2.0 license.

<br/>
