function getPhotos(photosData){
        $("#photos").empty();
       for (var i  in photosData){
            var photo = photosData[i];
            var elem = $("<img>");
            elem.attr("src", photo.filename);
            $("#photos").append(elem);
            $("#photos").append(photo.date);
            $("#photos").append(photo.interval);
            $("#photos").append(photo.isPublic);
        }
    }

    function getPhotosAjax(){
        $.get("/photos", getPhotos);

    }

    function getUser(userData) {
        if (userData.length == 0){
            $("#login").show();
        }
        else {
            $("#logout").show();
            $("#upload").show();
            $("#username").text(userData.username);
            getPhotosAjax();
            setInterval(getPhotosAjax, 3000);
        }
    }

    $.get("/user", getUser);