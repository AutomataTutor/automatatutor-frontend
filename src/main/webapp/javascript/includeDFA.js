var Editor = {
  curConfig: {
    dimensions: [740,480]
  }
};

function initCanvas() {
  if(Editor.canvas)
    return;
    Editor.canvas = new $.SvgCanvas("#svgcanvasdfa", Editor.curConfig, 'detaut'); 
}

$(document).ready(function() {
  initCanvas();
}); 

