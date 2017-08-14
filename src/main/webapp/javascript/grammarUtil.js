function formateGrammar(grammarString) {
	var res = grammarString.replace(/->/g, " -> ");
	res = res.replace(/=>/g, " -> ");
	res = res.replace(/\|/g, " | ");
	res = res.replace(/\s{2,}/g, " ")
	res = res.replace(/\s(?=\S+\s*->)/, "\n");
	return res;
}

function sanitizeInputForXML(id) {
	var input = document.getElementById(id);
	if (input === null) return "";
	input.value = input.value.replace(/[<>&]/g, "");
	return input.value;
}