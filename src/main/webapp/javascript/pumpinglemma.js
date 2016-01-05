/**
 * @file Interface for drawing pumping lemma symbolic string splits
 *
 * @author Loris D'Antoni [mweaver223@gmail.com]
 */
$.SvgCanvas = function(container, config) {

    var Utils = this.Utils = function() {

	var _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    }();
    	
	
    // set up SVG for D3
    width = config.dimensions[0];
    height = config.dimensions[1];

    var svg = d3.select(container)
	.append('svg')
	.attr('width', width)
	.attr('width', width)
	.attr('width', width)
	.attr('height', height);
		
	maxWidth=50; 
	lineColor = 'gray';
	brColor = 'blue';
	linesYCoord = 40;
	vertLineOff = 10;	
	bracketsVertCoord = 62; // Is y cord when flipped
	//var bracketsYCoord = 80; // Is y cord when flipped
	emptySpace = 15;
	measureUnit = ((width-(emptySpace*2))/maxWidth);
	textOffsetBr=22;
	textOffsetLn=10;
	
	scaleBracketHeight = 4;
	
	//Default, needs to be set
	//example();
	
	// Draws a symbolic string split from two sequences of JSon Objects
	// The first sequence contains the symbolic representation of the pumped string (e.g. a^p b^p as two separate strings)
	// The second sequence contains the coordinates of the points x y z
	function example(){			
		drawLine(0,25,'a^p');
		drawLine(25,50,'b^p');
		drawBrLine(0,30, 'x');	
		drawBrLine(30,40, 'y');	
		drawBrLine(40,50,'z');		
	}
	
	function drawBrLine(relFrom, relTo, label){
		
		//The plus one avoid overlapping of brackets
		var absFrom = emptySpace+relFrom*measureUnit+1;
		var absTo = emptySpace+relTo*measureUnit-1;
	
		var half = ((absTo-absFrom)/2)+absFrom;
	
		svgHorLine(absFrom+vertLineOff,half-vertLineOff, bracketsVertCoord, brColor);
		svgHorLine(half+vertLineOff,absTo-vertLineOff, bracketsVertCoord, brColor);
		
		pointBracket(half, bracketsVertCoord, brColor);
		circLine(absFrom , bracketsVertCoord, brColor, false);
		circLine(absTo , bracketsVertCoord, brColor, true);
		svgText(half,bracketsVertCoord+textOffsetBr,label);		
	}
	
	function drawLine(relFrom, relTo, label){
		
		var absFrom = emptySpace+relFrom*measureUnit;
		var absTo = emptySpace+relTo*measureUnit;
		
		var half = ((absTo-absFrom)/2)+absFrom;
	
		svgHorLine(absFrom,absTo, linesYCoord, lineColor);
		svgVerLine(absFrom , linesYCoord, lineColor);
		svgVerLine(absTo , linesYCoord, lineColor);
		svgText(half,linesYCoord-textOffsetLn,label);
	}
	
	function svgHorLine(f, t, vcord, color){
		svg.append('line')
			.attr('stroke', color)
			.attr('x1', f)
			.attr('x2', t)
			.attr('y1', vcord)			
			.attr('y2', vcord);
	}
	
	function circLine(c, vcord, color, isC){	
		var strwidth=1;
		if(isC)	
			svg.append('path')
				.attr('stroke', color)
				.attr('stroke-width',strwidth)
				.attr('d', 'M'+c+' '+(vcord-vertLineOff)+' Q '+c+' '+vcord+' '+(c-vertLineOff)+' '+vcord+'')
				.attr('fill', 'transparent');
		else
			svg.append('path')
				.attr('stroke', color)
				.attr('stroke-width',strwidth)
				.attr('d', 'M'+c+' '+(vcord-vertLineOff)+' Q '+c+' '+vcord+' '+(c+vertLineOff)+' '+vcord+'')
				.attr('fill', 'transparent');
	}
	function pointBracket(c, vcord, color){	
		var strwidth=1;
		svg.append('path')
				.attr('stroke', color)
				.attr('stroke-width',strwidth)
				.attr('d', 'M'+(c-vertLineOff)+' '+vcord+' Q '+c+' '+vcord+' '+c+' '+(vcord+vertLineOff)+'')
				.attr('fill', 'transparent');
		svg.append('path')
				.attr('stroke', color)
				.attr('stroke-width',strwidth)
				.attr('d', 'M'+(c+vertLineOff)+' '+vcord+' Q '+c+' '+vcord+' '+c+' '+(vcord+vertLineOff)+'')
				.attr('fill', 'transparent');
	}
	
	function svgVerLine(c, vcord, color){
		
			svg.append('line')
				.attr('stroke', color)
				.attr('x1', c)
				.attr('x2', c)
				.attr('y1', vcord-vertLineOff)			
				.attr('y2', vcord+vertLineOff);
	}
	
	
	function svgText(x, y, text){	
			svg.append('text')
				.attr('x', x)
				.attr('y', y)
				.attr('text-anchor','middle')
				.text(text);
	}
	
	 /**
     * Draws a single instance of a symbolic string from XML
     */
	 this.readXML = function (xml) {

		var xmlDoc = $('<wrap>'+xml+'</wrap>');
		maxWidth=0;		
	
		xmlDoc.find('string').each(
			function(){
				var t = parseInt($(this).find("to").text());
				if(t>maxWidth)
					maxWidth=t;
			}
		);
		
		measureUnit =((width-(emptySpace*2))/maxWidth);
		
		xmlDoc.find('string').each(
			function(){
				var f = parseInt($(this).find("from").text());
				var t = parseInt($(this).find("to").text());
				var label = $(this).find("label").text();
				drawLine(f, t, label);
			}
		);
		
		xmlDoc.find('split').each(
			function(){
				var f = parseInt($(this).find("from").text());
				var t = parseInt($(this).find("to").text());
				var label = $(this).find("label").text();
				drawBrLine(f, t, label);
			}
		);
	}
}
