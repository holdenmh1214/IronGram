package com.theironyard.controllers;

import com.theironyard.entities.Photo;
import com.theironyard.entities.User;
import com.theironyard.services.PhotoRepository;
import com.theironyard.services.UserRepository;
import com.theironyard.utils.PasswordHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by holdenhughes on 11/17/15.
 */
@RestController
public class IronGramController {
    @Autowired
    UserRepository users;
    @Autowired
    PhotoRepository photos;

    @RequestMapping("/login")
    public User login(HttpSession session, HttpServletResponse response, String username, String password)
            throws Exception {
        User user =users.findOneByUsername(username);

        if (user == null){
            user = new User();
            user.username = username;
            user.password= PasswordHash.createHash(password);
            users.save(user);
        }
        else if(!PasswordHash.validatePassword(password, user.password)){
            throw new Exception("Wrong password");
        }

        session.setAttribute("username", username);
        response.sendRedirect("/");


        return user;
    }

    @RequestMapping("/logout")
    public void logout(HttpSession session, HttpServletResponse response) throws IOException {
        session.invalidate();
        response.sendRedirect("/");
    }

    @RequestMapping("/user")
        public User user(HttpSession session){
            String username = (String) session.getAttribute("username");
            if (username == null){
                return null;
        }
        return users.findOneByUsername(username);
    }

    @RequestMapping("/upload")
    public Photo upload(HttpSession session,
                        HttpServletResponse response,
                        String receiver,
                        MultipartFile photo,
                        int interval,
                        boolean isPublic
                        ) throws Exception {
        String username =(String) session.getAttribute("username");
        if (username == null){
            throw new Exception("Not logged in");
        }

        User senderUser = users.findOneByUsername(username);
        User receiverUser = users.findOneByUsername(receiver);

        if (receiverUser == null){
            throw new Exception("Receiver does not exist");
        }

        if (!photo.getContentType().startsWith("image")){
            throw new Exception("Only images are allowed");
        }

        File photoFile = File.createTempFile("photo", photo.getOriginalFilename(), new File("public"));
        FileOutputStream fos = new FileOutputStream(photoFile);
        fos.write(photo.getBytes());

        Photo p = new Photo();
        p.date = LocalDateTime.now().toString();
        p.timestamp = LocalDateTime.now();
        p.interval = interval;
        p.isPublic = isPublic;
        p.sender = senderUser;
        p.receiver = receiverUser;
        p.filename = photoFile.getName();
        photos.save(p);

        response.sendRedirect("/");
        return p;
    }

    @RequestMapping("/photos")
    public List<Photo> showPhotos(HttpSession session) throws Exception {
        String username = (String) session.getAttribute("username");
        if (username == null){
            throw new Exception("not logged in");
        }

        User user = users.findOneByUsername(username);

        List<Photo> photosL = photos.findByReceiver(user);

        for (Photo p: photosL) {
            if (LocalDateTime.now().minusSeconds(p.interval).isAfter(p.timestamp)) {
                photos.delete(p);
            }
        }

        return photos.findByReceiver(user);
    }

    @RequestMapping("/public-photos")
    public List<Photo> publicPhotos(String username){
        User user = users.findOneByUsername(username);

        ArrayList<Photo> pubList = new ArrayList<>();
        for (Photo photo: photos.findBySender(user)){
            if (photo.isPublic=true){
                pubList.add(photo);
            }
        }
        return pubList;
    }

}
