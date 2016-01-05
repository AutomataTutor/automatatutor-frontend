var Editor = {
  curConfig: {
    dimensions: [740,480]
  }
};

function initCanvas() {
  if(!Editor.canvasNfa) {
      Editor.canvasNfa = new $.SvgCanvas("#svgcanvasnfa", Editor.curConfig, false); 
  }
  if(!Editor.canvasDfa) {
      Editor.canvasDfa = new $.SvgCanvas("#svgcanvasdfa", Editor.curConfig, true); 
  }
}

$(document).ready(function() {
  initCanvas();
}); 


