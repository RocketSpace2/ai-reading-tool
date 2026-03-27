# AI Reading Tool

## Content
- [Overview](#overview)
- [Architecture](#architecture)
- [Setup and Running](#setup-and-running)
- [API Endpoints](#api-endpoints)

## Overview

AI Reading Tool is a API that allows user to upload source materials and generate AI-based notes from them.  
The application integrates with Gemini for note generation and with MinIO as S3-compatible object storage for saving generated documents.

The main goal of the project is to automate the process of turning provided content into structured notes and storing them in an organized way.

## Architecture

The project uses a layered architecture. Its purpose is to separate HTTP handling, application orchestration, external integrations, and storage logic.

### Main parts of the project

1. **Presentation layer**
    - Contains REST controllers and request/response DTOs.
    - Responsible for receiving HTTP requests and returning API responses.

2. **Application layer**
    - Contains the main business orchestration logic.
    - Coordinates note generation, storage decisions, and document-related operations.

3. **Infrastructure**
    - Responsible for communication with the Gemini API.
    - Sends file/content data to the model and receives generated notes together with metadata such as folder or file name suggestions.

4. **Storage layer**
    - Handles saving, loading, listing, and deleting files in MinIO.
    - Uses S3-compatible APIs.

5. **Configuration**
    - Contains configuration classes for external services such as MinIO/S3 and Gemini.
    - Also includes application startup configuration where needed.

### Important project files/classes

- **AIReadingToolApplication**
    - Main Spring Boot entry point.

- **Controller classes**
    - Expose the REST API endpoints for uploading files, listing saved documents, downloading them, and deleting them.

- **DocumentService**
    - Handles file operations related to MinIO, such as upload, download, listing files, deleting files, and bucket-related logic.

- **GeminiClient**
    - Responsible for sending content to Gemini and receiving generated notes.

- **NotesService**
    - Coordinates the full process: receiving input, generating notes, and saving the result.

- **DTO**
    - Used to keep API communication clear and separated from internal logic.

## Setup and Running

### 1. Clone the repository

```bash
git clone https://github.com/RocketSpace2/ai-reading-tool.git
```

### 3. Build the project

To build the application jar without running tests:

```bash
mvn clean package -DskipTests
```

### 4. Run the project with Docker

Start both the Spring Boot application and MinIO with one command:

```bash
docker compose up --build
```

To run in detached mode:

```bash
docker compose up --build -d
```

### 5. Stop the containers

```bash
docker compose down
```

### 6. Access points

* Spring Boot API: `http://localhost:8080`
* MinIO API: `http://localhost:9000`
* MinIO Console: `http://localhost:9001`

Default MinIO credentials:

* Login: `minioadmin`
* Password: `minioadmin`

## API Endpoints

### Generate notes from uploaded file

**POST** `/notes/generate`

Uploads a file, sends it to Gemini, generates notes, and stores the final result in MinIO.

Expected behavior:

* accepts the uploaded file,
* generates structured notes,
* chooses or suggests a storage path,
* saves the resulting document in object storage,
* returns note-related metadata in the response.

### List stored files

**GET** `/documents/files`

Returns the list of files currently stored in MinIO.

Expected behavior:

* lists available files or folders,
* returns document metadata needed by the frontend or client.

### Download stored file

**GET** `/documents/download?objectKey={objectKey}`

Downloads a file from MinIO using its object key.

Expected behavior:

* validates the provided object key,
* fetches the file from storage,
* returns the file as a downloadable response.

### Delete stored file

**DELETE** `/documents/delete?objectKey={objectKey}`

Deletes a file from MinIO using its object key.

Expected behavior:

* validates the provided object key,
* removes the file from storage,
* returns confirmation about the deletion.