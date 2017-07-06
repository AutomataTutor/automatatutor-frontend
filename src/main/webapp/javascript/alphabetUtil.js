function sanitizeStringArray(stringArray) {
		var sanitizedArray = new Array();
		for(var i = 0; i < stringArray.length; i++){
			if (stringArray[i]){
				sanitizedArray.push(stringArray[i]);
			}
		}
		return sanitizedArray;
	}

function parseAlphabet() {
	var rawInput = document.getElementById('alphabetField').value;
	//var inputWithoutSpaces = rawInput.replace(/\s+/g, '');
	var splitInput = rawInput.split(" ");
	var alphabetWithoutEmptyEntries = sanitizeStringArray(splitInput)
	return alphabetWithoutEmptyEntries;
}	

function parseAlphabetByFieldName(fieldName) {
	var rawInput = document.getElementById(fieldName).value;
	//var inputWithoutSpaces = rawInput.replace(/\s+/g, '');
	var splitInput = rawInput.split(" ");
	var alphabetWithoutEmptyEntries = sanitizeStringArray(splitInput)
	return alphabetWithoutEmptyEntries;
}	

function alphabetChecks(stringArray){
	var tmpArray = [];
	for(var i = 0; i < stringArray.length; i++){
		var elem = stringArray[i];
		if (elem.length>1){	
			alert("'"+elem+"' is not a character.\nThe alphabet cannot contain strings.");
			return false;
		}
		if(tmpArray.indexOf(elem)>=0){
			alert("The character '"+elem+"' is repeated twice.");
			return false;
		}
		tmpArray.push(elem);
	}
	return true;
}

function multipleAlphabetChecks(){
	if (arguments.length == 0) return true;
	var concatenated = arguments[0];
	var i;
	for(i = 1; i < arguments.length; i++) concatenated.concat(arguments[i]);
	return alphabetChecks(concatenated);
}		