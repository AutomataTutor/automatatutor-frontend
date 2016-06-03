var Editor = {
  curConfig: {
    dimensions: [740,480]
  }
};

function initCanvas() {
  if(Editor.canvas)
    return;
    Editor.canvas = new $.SvgCanvas("#svgcanvasbuchigame", Editor.curConfig, 'buchigame'); 
    Editor.canvas.setAlphabet(['i,\u03B5'])
}

$(document).ready(function() {
  initCanvas();
}); 

