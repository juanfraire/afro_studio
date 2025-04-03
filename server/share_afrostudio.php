<?php

/*
Table: ensembles - (id, ensemblename, ensembledate, ensembleauthor, ensembleuser_id, ensemblejson)
Table: users, - (id, email, password, date_created, verified)
Table: log,
*/

$host="localhost";
$usuario="user";
$pwd="pwd";
$base_de_datos="db";

$link = mysql_connect($host, $usuario, $pwd) or die("Could not connect to DB:".mysql_error());
mysql_select_db($base_de_datos, $link); 

$ensembleName = "-";
$ensembleAuthor = "-";

// Get and check Username (register)
$userName = $_POST["username"];
$result = mysql_query("SELECT id FROM users WHERE email = '$userName'")or die ("user_error: ".mysql_error()); //returns a resource (Resource id #3) if true, or false if error.
if(mysql_num_rows($result) == 0) { // user not found, create new?
	$result = mysql_query("INSERT INTO users (email) VALUES ('$userName')") or die ("user_error: ".mysql_error());
	$result = mysql_query("SELECT id FROM users WHERE email = '$userName'")or die ("user_error: ".mysql_error());
}
$user = mysql_fetch_row($result); // user[0]=ensembleuser_id
$ensembleUser = $user[0];

// Get and check Action
$action = $_POST["action"];
switch ($action) {
    case "setEnsemble":
		$ensembleName = $_POST["ensemblename"];
		$ensembleAuthor = $_POST["ensembleauthor"];
		$ensembleJSON = $_POST["ensemblejson"];
		$result = mysql_query("DELETE FROM ensembles WHERE ensemblename = '$ensembleName' AND ensembleauthor = '$ensembleAuthor' AND ensembleuser_id = '$ensembleUser'") or die ("setEnsemble_error: ".mysql_error());
		$result = mysql_query("INSERT INTO ensembles (ensemblename, ensembleauthor, ensembleuser_id, ensemblejson) VALUES ('$ensembleName', '$ensembleAuthor', '$ensembleUser' , '$ensembleJSON')")or die ("setEnsemble_error: ".mysql_error());
		echo "setEnsemble_ok";	
		break;		
	case "deleteEnsemble":
		$ensembleName = $_POST["ensemblename"];
		$ensembleAuthor = $_POST["ensembleauthor"]; 
		// $ensembleUser -> id
		$result = mysql_query("DELETE FROM ensembles WHERE ensemblename = '$ensembleName' AND ensembleauthor = '$ensembleAuthor' AND ensembleuser_id = '$ensembleUser'") or die ("setEnsemble_error: ".mysql_error());
		
		// Now return the ensemble list again to populate the dialog
		// Local User first
		$result = mysql_query("SELECT ensemblename, ensembleauthor, ensembleuser_id FROM ensembles WHERE ensembleuser_id = '$ensembleUser' ")or die ("getEnsembleList_error: ".mysql_error());		
		while ($row = mysql_fetch_assoc($result)) {
			$user = $row["ensembleuser_id"];
			$resultUser = mysql_query("SELECT email FROM users WHERE id = '$user'") or die ("getEnsembleList_error: ".mysql_error());		
			$user = mysql_fetch_row($resultUser);
			$user = explode("@", $user[0], 2);
			$user = $user[0]; // user without the @...
			echo $row["ensemblename"]."_by_".$row["ensembleauthor"]."_u_".$user."+";
		}		
		// All Users then
		$result = mysql_query("SELECT ensemblename, ensembleauthor, ensembleuser_id FROM ensembles WHERE ensembleuser_id != '$ensembleUser'")or die ("getEnsembleList_error: ".mysql_error());		
		while ($row = mysql_fetch_assoc($result)) {
			$user = $row["ensembleuser_id"];
			$resultUser = mysql_query("SELECT email FROM users WHERE id = '$user'") or die ("getEnsembleList_error: ".mysql_error());		
			$user = mysql_fetch_row($resultUser);
			$user = explode("@", $user[0], 2);
			$user = $user[0]; // user without the @...
			echo $row["ensemblename"]."_by_".$row["ensembleauthor"]."_u_".$user."+";
		}
		break;
	case "getEnsemble":
		$ensembleName = $_POST["ensemblename"];
		$ensembleAuthor = $_POST["ensembleauthor"];
		//$ensembleUser = $_POST["ensembleuser"]."@gmail.com";
		$ensembleUser = $_POST["ensembleuser"];
		//$resultUser = mysql_query("SELECT id FROM users WHERE email = '$ensembleUser'") or die ("getEnsembleList_error: ".mysql_error());
		$resultUser = mysql_query("SELECT id FROM users WHERE email LIKE '$ensembleUser%'") or die ("getEnsembleList_error: ".mysql_error()); //Busca que contenga el username@xxx y no @gmail.com
		$ensembleUserId = mysql_fetch_row($resultUser);
		$ensembleUserId = $ensembleUserId[0];
		$result = mysql_query("SELECT ensemblejson FROM ensembles WHERE ensemblename = '$ensembleName' AND ensembleauthor = '$ensembleAuthor' AND ensembleuser_id = '$ensembleUserId'")or die ("getEnsembleList_error: ".mysql_error());
		$ensembleJSON = mysql_fetch_row($result);
		echo $ensembleJSON[0];
        break;
	case "getEnsembleList":
		// Local User first
		$result = mysql_query("SELECT ensemblename, ensembleauthor, ensembleuser_id FROM ensembles WHERE ensembleuser_id = '$ensembleUser' ")or die ("getEnsembleList_error: ".mysql_error());		
		while ($row = mysql_fetch_assoc($result)) {
			$user = $row["ensembleuser_id"];
			$resultUser = mysql_query("SELECT email FROM users WHERE id = '$user'") or die ("getEnsembleList_error: ".mysql_error());		
			$user = mysql_fetch_row($resultUser);
			$user = explode("@", $user[0], 2);
			$user = $user[0]; // user without the @...
			echo $row["ensemblename"]."_by_".$row["ensembleauthor"]."_u_".$user."+";
		}		
		// All Users then
		$result = mysql_query("SELECT ensemblename, ensembleauthor, ensembleuser_id FROM ensembles WHERE ensembleuser_id != '$ensembleUser'")or die ("getEnsembleList_error: ".mysql_error());		
		while ($row = mysql_fetch_assoc($result)) {
			$user = $row["ensembleuser_id"];
			$resultUser = mysql_query("SELECT email FROM users WHERE id = '$user'") or die ("getEnsembleList_error: ".mysql_error());		
			$user = mysql_fetch_row($resultUser);
			$user = explode("@", $user[0], 2);
			$user = $user[0]; // user without the @...
			echo $row["ensemblename"]."_by_".$row["ensembleauthor"]."_u_".$user."+";
		}
        break;
	default:
		break;
}

// Register User Activity
$result = mysql_query("INSERT INTO log (username, ensemblename, ensembleauthor, action) VALUES ('$userName', '$ensembleName', '$ensembleAuthor', '$action')") or die ("log_error: ".mysql_error());

?>