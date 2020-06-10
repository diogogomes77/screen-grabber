package dev.dgomes.backend.fileupload;

import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

        List<FileInfo> fileInfos = storageService.loadAll().map(
                path -> {
                    String filename = path.getFileName().toString();
                    String url = MvcUriComponentsBuilder
                            .fromMethodName(FileUploadController.class,"serveFile",path
                                    .getFileName()
                                    .toString())
                            .build()
                            .toUri()
                            .toString();
                    return new FileInfo(filename, url);
                }
        ).collect(Collectors.toList());

        model.addAttribute("files", fileInfos);
        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(
            final HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        if (!isMultipart) {
            throw new StorageException("File not Multipart as expected!");
        }

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload();

        FileItemIterator iter;
        InputStream fileStream = null;
        String name = null;
        try {
            iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                name = item.getName();
                fileStream = item.openStream();

                if (!item.isFormField()) {
                    System.out.println("File field " + name + " with file name " + item.getName() + " detected.");
                    break;
                }
            }
        } catch (FileUploadException | IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", name + " upload error!");
            return "redirect:/";
        }
        if (fileStream != null) {
            System.out.println("fileStream = " + fileStream.toString());
            storageService.store_stream(fileStream, name);
        }
        redirectAttributes.addFlashAttribute("message", name + " uploaded successfully!");
        return "redirect:/";
    }


}