var Editor = {
  curConfigDfa1: {
    dimensions: [740,480]
  },
  curConfigDfa2: {
    dimensions: [740,480]
  }
};

function initCanvas() {
  if(!Editor.canvasDfa1) {
      Editor.canvasDfa1 = new $.SvgCanvas("#svgcanvasdfa1", Editor.curConfigDfa1, 'detaut');
  }
  if(!Editor.canvasDfa2) {
        Editor.canvasDfa2 = new $.SvgCanvas("#svgcanvasdfa2", Editor.curConfigDfa2, 'detaut');
    }
}

$(document).ready(function() {
  initCanvas();
}); 


