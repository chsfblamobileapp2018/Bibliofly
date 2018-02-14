/*
This script reads the file from a certain file path 
returns the contents of the file as a string.
*/
document.getElementById("myBtn").addEventListener("click", function() {

  //create new file reader.
  var reader = new FileReader();

  //set a callback for the reader so this block of code runs after the reader has loaded the text.
  reader.addEventListener('load', function() {
    document.getElementById('file').innerText = this.result;

    //split the csv by line.
    var splitted = this.result.split(/\n|\r/);

	// Get a reference to the database service
	var database = firebase.database();

	// Loop through all lines in the csv.
	for (let j = 0; j < splitted.length; j++) {
		//parse the line.

    //split each line by , to get the individual values per row.
		var keyVal = splitted[j].split(",");
		//get the BARCODE value which corresponds to the 0th index.
    var BARCODE = keyVal[0];
    //get the ISBN_VAL which corresponds to the 1st element in the array
		var ISBN_VAL = keyVal[1];

    //log out the vars so we can see them.
		console.log("Barcode = " + BARCODE + " & " + "ISBN = " + ISBN_VAL);

		//push BARCODE & ISBN to Firebase
		database.ref('Barcodes/' + BARCODE).set({
		    ISBN: ISBN_VAL
		  });
        //use JQuery to access the google books API and figure out what the book, title, author, description, url, and rating. 
        $.get("https://www.googleapis.com/books/v1/volumes?q=isbn:" + ISBN_VAL, function(response) {
            var book = response.items[0];
            var title = book.volumeInfo.title;
            var author = book.volumeInfo.authors[0];
            var description = book.volumeInfo.description;
            var url = book.volumeInfo.imageLinks.smallThumbnail;
            var rating = book.volumeInfo.averageRating;
            
            if (rating === undefined) {
              rating = 0;
            }
            
            console.log("Book: " + title + " " + author + " " + url + " " + rating);

            var BLOCK_SAFE_DATA = splitted[j].split(",");
            //var BLOCK_SAFE_BARCODE = BLOCK_SAFE_DATA[0];
            var BLOCK_SAFE_ISBN = BLOCK_SAFE_DATA[1];

            console.log("Block safe ISBN: " + BLOCK_SAFE_ISBN);

            //add the information to the firebase database under the Books child. 
            database.ref('Books/' + BLOCK_SAFE_ISBN).set({
                Author: author,
                Description: description,
                Rating: rating,
                Title: title,
                URL: url
            });
        });
        
        
	}

    //Set the total number of books under the statistics child. 
    firebase.database().ref('Statistics/TotalBooksInLibrary').once('value').then(function(snapshot) {
        var currentNumberOfBooks = snapshot.val()
        //the number of books is the length of the splitted array (num of rows).
        database.ref('Statistics/TotalBooksInLibrary').set(currentNumberOfBooks + splitted.length)
    });


   //alert the user that all of the books were added. 
	 alert("Added all books to firebase. You're all set up! :)")


  });

    //call the callback so we can begin loading.
    reader.readAsText(document.querySelector('input').files[0]);

});






document.getElementById("myBtn2").addEventListener("click", function() {
  //create a reader object.
  var reader = new FileReader();

  reader.addEventListener('load', function() {
    document.getElementById('file2').innerText = this.result;

    var splitted = this.result.split(/\n|\r/);

  // Get a reference to the database service
  var database = firebase.database();

  //iterate over all of the lines in the csv file. 
  
  //note j must be declared as LET because we need to preserve the value of j when we go into the js closure. 
  for (let j = 0; j < splitted.length; j++) {

    //get an array of values representing the row. 
    let data = splitted[j].split(",");
    //email is indexed at the 0th element. 
    let EMAIL = data[0];
    //password is 1st index of array
    let PASSWORD = data[1];
    //name is next in the array
    let NAME = data[2];
    //status is last in the array.
    let STATUS = data[3];

    console.log("Email: " + EMAIL + " PASSWORD: " + PASSWORD + " NAME: " + NAME + " STATUS: " + STATUS);
      
      //create firebase user with given credentials. 
      firebase.auth().createUserWithEmailAndPassword(EMAIL, PASSWORD)
      .then(function(user){
        console.log('uid',user.uid)

        //block safe data is data which is preserved inside of the closure we are in. 
        var BLOCK_SAFE_DATA = splitted[j].split(",");
        var BLOCK_SAFE_NAME = BLOCK_SAFE_DATA[2];
        var BLOCK_SAFE_STATUS = BLOCK_SAFE_DATA[3];

        console.log("j = " + j + " name = " + BLOCK_SAFE_NAME + " status = " + BLOCK_SAFE_STATUS);

        //set user name and status in the database given the user.uid.
        firebase.database().ref("Users/" + user.uid).set({
          Name: NAME,
          Status: STATUS
        }); 
        //Here if you want you can sign in the user
      }).catch(function(error) {
          //Handle error
          var errorCode = error.code;
          var errorMessage = error.message;
          console.log("Error: " + errorCode + " " + errorMessage);
      });
  }


  

  alert("Created all users. You're all set up! :)")


  });

    //call the reader callback to tell it to begin parsing the .csv file. 
    reader.readAsText(document.getElementById('file2').files[0]);

});



function logout(){
  firebase.auth().signOut();
  window.location.href = "index.html"
}


