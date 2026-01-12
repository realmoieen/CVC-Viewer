# CVC-Viewer

**Author:** Moieen Abbas

---

## Description

**CVC-Viewer** is a desktop utility to view and inspect **Card Verifiable Certificates (CVC)**.
Card Verifiable Certificates are specialized digital certificates designed to be processed by constrained devices such
as **smart cards**.

CVCs are defined in **BSI TR-03110** and are widely used in the **EU EAC (Extended Access Control)** ecosystem for:

* ePassports
* eIDs
* Inspection Systems
* Terminal and DV/IS certificates

This tool provides a **human-readable view** of CV Certificates without assuming X.509 semantics.

---

## Features

* View **CVC (BSI TR-03110)** certificates
* Supports **raw DER**, **Base64**, and **PEM-like** inputs
* Displays:

    * Certificate Holder Reference (CHR)
    * Certificate Authority Reference (CAR)
    * Public Key parameters
    * Validity dates
    * Authorization roles and permissions
* Supports **single certificates** and **certificate chains**

---
## How to Use CVC-Viewer

### Windows Environment

The **GitHub Releases** page provides a ready-to-use **Windows ZIP distribution**.

#### Steps:

1. Download the **Windows ZIP** from GitHub Releases
2. Extract the ZIP archive
3. Run the EXE file:

   ```
   CVCViewer.exe
   ```
4. A **file chooser dialog** will appear
5. Select:

    * CV Certificate
    * CV Request
    * Related EAC/CVC files

The certificate details will be displayed in the viewer.

#### Java Requirement (Windows)

* Java **must be installed**
* OR `JAVA_HOME` must be correctly set in system environment variables
* **Minimum required JRE:**

  ```
  Java 1.8.0_131
  ```

---

### Linux & macOS Environment

For Linux and macOS, use the **executable JAR** provided in GitHub Releases. 

⚠️ This application required Graphical Environment to run, headless is not supported.

#### Option 1: Launch with File Chooser

```bash
java -jar <location>/CVC-Viewer-2.0.jar
```

This will open the application and prompt you to select a certificate file.

#### Option 2: Open Certificate Directly

```bash
java -jar <location>/CVC-Viewer-2.0.jar <certificate_path>
```

This will launch the viewer and **directly load the specified CV certificate**.

#### Java Requirement (Linux & macOS)

* Java Runtime Environment installed
* **Minimum required JRE:**

  ```
  Java 1.8.0_131
  ```

---

## Windows Integration (Context Menu Support)

The **Windows ZIP distribution** contains an additional registry file:

```
CVC_Registry.reg
```

This registry file **adds CVC-Viewer to the Windows right-click context menu**, allowing you to open CV certificates directly by right-clicking on a file.

### Important Notes ⚠️

* The registry entry creates a **direct link to the CVC-Viewer EXE file**
* **Do NOT move or rename the EXE after applying the registry**

    * Doing so will **break the context-menu link**
* If you move the EXE, you must **re-apply the registry file**

### How to Register the Context Menu

1. Extract the Windows ZIP file
2. Ensure the EXE file is placed in its **final location**
3. Double-click:

   ```
   CVC_Registry.reg
   ```
4. Confirm the Windows registry security dialog

After registration, you can right-click a CV certificate file and open it directly using **CVC-Viewer**.

---

## Certificate Parsing Logic

CVC-Viewer uses a **robust and format-agnostic certificate parser** designed specifically for CV Certificates.

### Parsing Order

When a certificate file is loaded, the viewer applies the following logic:

1. **PEM with headers**
   If the file contains headers such as:

   ```
   -----BEGIN CERTIFICATE-----
   ```

   the Base64 content is extracted and decoded.

   **Supported PEM Headers Types:**
   ```
    TYPE_CERTIFICATE = "CERTIFICATE";
    TYPE_CV_CERTIFICATE = "CV CERTIFICATE";
    TYPE_CV_LINK_CERTIFICATE = "CV LINK CERTIFICATE";
    TYPE_CV_REQUEST = "CV REQUEST";
    TYPE_CV_AUTHENTICATED_REQUEST = "CV AUTHENTICATED REQUEST";
    ```

2. **Multiple PEM certificates**
   If multiple `BEGIN CERTIFICATE` blocks are found, **all certificates are parsed** and loaded as a list (certificate
   chain support).

3. **Comma-separated Base64 certificates**
   If no headers are present but the file contains multiple Base64 blobs separated by commas, each entry is decoded as a
   separate CV certificate.

4. **Plain Base64 certificate**
   If the content looks like Base64 without headers, it is decoded directly.

5. **Raw binary certificate (DER / CV)**
   If none of the above formats match, the file is treated as **raw binary CV certificate data** and loaded as-is.

This ensures **full compatibility with BSI TR-03110** encoded certificates.

---

## Supported Use Cases

CVC-Viewer supports the following real-world scenarios:

* Viewing **Terminal Certificates (AT / IS / DV)**
* Inspecting **certificate chains** (e.g., CVCA → DV → Terminal)
* Debugging **EAC-based systems**
* Analyzing certificates extracted from:

    * Smart cards
    * HSMs
    * ePassport inspection systems
    * File-based test vectors
* Educational and diagnostic use for **PKI / EAC / ICAO** implementations

---

## Screenshots

### General Tab

![](https://raw.githubusercontent.com/realmoieen/CVC-Viewer/master/src/main/resources/screenshot1.png)

### Detail Tab

![](https://raw.githubusercontent.com/realmoieen/CVC-Viewer/master/src/main/resources/screenshot2.png)

---

## How to Build CVC-Viewer

```bash
git clone https://github.com/realmoieen/CVC-Viewer.git
cd CVC-Viewer
./mvnw install
```

---

## Acknowledgments

This project includes the **Bouncy Castle cryptographic libraries**, available from:

* [http://www.bouncycastle.org/](http://www.bouncycastle.org/)

This project also includes a **CVC module** providing full support for **Card Verifiable Certificates (BSI TR-03110)**
used by EU EAC ePassports and eIDs:

* [https://github.com/eID-Testbeds/common-testbed-utilities](https://github.com/eID-Testbeds/common-testbed-utilities)

---

## License

Refer to the repository for licensing details.
