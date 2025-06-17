import java.io.File
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/files")
class FileListController {

    @GetMapping("/list")
    fun listFiles(): ResponseEntity<List<String>> {
        val dir = File(System.getProperty("java.io.tmpdir") + "/secure-media-vault")
        return if (dir.exists() && dir.isDirectory) {
            val files = dir.listFiles()?.map { it.name } ?: emptyList()
            ResponseEntity.ok(files)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(emptyList())
        }
    }
}
