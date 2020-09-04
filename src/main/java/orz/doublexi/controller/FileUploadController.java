package orz.doublexi.controller;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {

    @RequestMapping("/getlist")
    @ResponseBody
    public List listUploadedFiles(Model model) throws IOException {
        return getFileList();
    }

    @RequestMapping("/file/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = new FileSystemResource(getPath() + filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @RequestMapping("/upload")
    @ResponseBody
    public Map<String, String> handleFileUpload(@RequestParam("file") MultipartFile file,
                                RedirectAttributes redirectAttributes) {
        String fileName = UUID.randomUUID().toString().substring(0,5)+"_"+file.getOriginalFilename();
        try {
            file.transferTo(new File(getPath()+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        HashMap<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("msg", "OK上传成功");

        return response;
    }
    private List getFileList(){
        File file = new File(getPath());
        File[] files = file.listFiles();
        List<File> fileList = Arrays.asList(files);
        List<HashMap> collect = fileList.stream().sorted(Comparator.comparing(File::lastModified)).map(f -> {
            HashMap map = new HashMap();
            map.put("name", f.getName());
            map.put("size", String .format("%.2f",f.length()/1024d)+" KB");
            map.put("modifytime", DateFormatUtils.ISO_DATE_FORMAT.format(new Date(f.lastModified())));
            return map;
        }).collect(Collectors.toList());
        return collect;
    }
    private String getPath(){
        String contextPath = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getServletContext().getRealPath("/file/");
        return contextPath;
    }


}