var Editor = {
  curConfig: {
    dimensions: [740,480]
  }
};

function initCanvas() {
  if(Editor.canvas)
    return;
    Editor.canvas = new $.SvgCanvas("#svgcanvasnfa", Editor.curConfig, false); 
}

$(document).ready(function() {
  initCanvas();
}); 

