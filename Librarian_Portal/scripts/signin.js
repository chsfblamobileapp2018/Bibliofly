function login(){

	//we set the username, and password combination to the value of the email, password input fields
	var username = document.getElementById('email').value;
	var password = document.getElementById('password').value;

	firebase.auth().signInWithEmailAndPassword(username, password).then(function(user) {
		
		//check to make sure that the user is a librarian
		firebase.database().ref('/Users/' + firebase.auth().currentUser.uid).once('value').then(function(snapshot) {
			
			//only librarians can access the portal so we need to make sure that the status of the person logged in is a librarian
			var status = snapshot.val().Status;

			//print out the status of the user for debugging.
			console.log("Status: " + status);


			if(status === "Student") {
				//this person is not a librarian and thus should not be logged in
				alert("You are not a librarian and should not be logged in");
    			//sign out the user once they press the logout button.
    			firebase.auth().signOut();
    			//reset the email and password fields.
    			document.getElementById('email').value = "";
    			document.getElementById('password').value = "";

			} else {
				// console.log("Successfully signed in");
				//the user is indeed a librarian so we need to go to the portal.
				window.location.href = "portal.html"
			}

		});




	}).catch(function(error) {
	    alert("Incorrect Username Or Password");
	});



};

