var Editor = {
  curConfig: {
    dimensions: [730,100]
  }
};

function initCanvas(canvasName, xmlString) {
	/*if(Editor.canvas)
		return;*/
	Editor.canvas = new $.SvgCanvas('#'+canvasName, Editor.curConfig); 
	var example = "<symbstr><strings><string><from>0</from><to>10</to><label>a^p</label></string><string><from>10</from><to>20</to><label>b^p</label></string></strings>";
	example += "<splits><split><from>0</from><to>10</to><label>x</label></split><split><from>10</from><to>15</to><label>y</label></split><split><from>15</from><to>20</to><label>z</label></split></splits>";
	example += "</symbstr>"
	Editor.canvas.readXML(xmlString);	
}

$(document).ready(function() {
  initCanvas();
}); 

