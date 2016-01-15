var Editor = {
  curConfig: {
    dimensions: [740,480]
  }
};

function initCanvas() {
  if(!Editor.canvasNfa) {
      Editor.canvasNfa = new $.SvgCanvas("#svgcanvasnfa", Editor.curConfig, 'nondetaut'); 
  }
  if(!Editor.canvasDfa) {
      Editor.canvasDfa = new $.SvgCanvas("#svgcanvasdfa", Editor.curConfig, 'detaut'); 
  }
}

$(document).ready(function() {
  initCanvas();
}); 


