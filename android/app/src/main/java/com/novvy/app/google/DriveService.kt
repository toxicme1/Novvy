package com.novvy.app.google

import com.novvy.app.domain.model.DriveFile

/**
 * Google Drive integration boundary.
 *
 * A real implementation uploads to
 * https://www.googleapis.com/upload/drive/v3/files with an OAuth token and can
 * create folders under /Novvy. The default [DemoDriveService] records the save
 * locally so the Drive save flow (with approval + progress) is fully demoable.
 */
interface DriveService {
    suspend fun ensureFolders(): Result<Unit>
    suspend fun save(file: DriveFile, bytes: ByteArray): Result<DriveFile>
}

class DemoDriveService : DriveService {
    override suspend fun ensureFolders(): Result<Unit> = Result.success(Unit)

    override suspend fun save(file: DriveFile, bytes: ByteArray): Result<DriveFile> {
        // Real impl: create folder if missing, then resumable upload with progress.
        return Result.success(file.copy(uploaded = true, sizeBytes = bytes.size.toLong()))
    }
}

/** Standard Novvy folder layout in Drive. */
object NovvyFolders {
    val businessProposals = "/Novvy/Business/Proposals/"
    val travel = "/Novvy/Travel/Itineraries/"
    val study = "/Novvy/Study/Notes/"
    val work = "/Novvy/Work/Reports/"
    val finance = "/Novvy/Finance/Invoices/"
    val all = listOf(businessProposals, travel, study, work, finance)
}