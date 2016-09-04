var Editor = {
  curConfigNfa: {
    dimensions: [740,480]
  },
  curConfigDfa: {
    dimensions: [740,480]
  }
};

function initCanvas() {
  if(!Editor.canvasNfa) {
      Editor.canvasNfa = new $.SvgCanvas("#svgcanvasnfa", Editor.curConfigNfa, 'nondetaut'); 
  }
  if(!Editor.canvasDfa) {
      Editor.canvasDfa = new $.SvgCanvas("#svgcanvasdfa", Editor.curConfigDfa, 'detaut'); 
  }
}

$(document).ready(function() {
  initCanvas();
}); 


