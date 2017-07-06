function formateGrammar(grammarString) {
	var res = grammarString.replace(/->/g, " -> ");
	res = res.replace(/=>/g, " -> ");
	res = res.replace(/\|/g, " | ");
	res = res.replace(/\s{2,}/g, " ")
	res = res.replace(/\s(?=\S+\s*->)/, "\n");
	return res;
}