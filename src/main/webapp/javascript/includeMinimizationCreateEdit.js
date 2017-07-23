var Editor = {
  curConfigDfa: {
    dimensions: [740,480]
  }
};

function initCanvas() {
  if(!Editor.canvas) {
      Editor.canvas = new $.SvgCanvas("#svgcanvasdfa", Editor.curConfigDfa, 'detaut');
  }
}

$(document).ready(function() {
  initCanvas();
}); 


