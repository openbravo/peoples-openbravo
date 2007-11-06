/**************************************************
Original Version (1.0):
Glenn G. Vergara
http://www21.brinkster.com/gver/
glenngv AT yahoo DOT com
Makati City, Philippines

Object-Based Version:
Eric C. Davis
http://www.10mar2001.com/
eric AT 10mar2001 DOT com
Atlanta, GA, US

(Keep the above intact if you want to use it! Thanks.)

Current Version: 2.3
Last Update: 11 September 2003
**************************************************/
String.fromKeyCode = function(keyCode,evtType) {
	if (!evtType || !evtType.length)
		evtType = "keyDown";
	else if (evtType.toLowerCase() == "keypress")
		return String.fromCharCode(keyCode);
	var keyDownChars = new Array(16);
		keyDownChars[8] = '[Bksp]';
		keyDownChars[9] = '[Tab]';
		keyDownChars[12] = '[N5+shift]';
		keyDownChars[13] = '[Enter]';
		keyDownChars.push('[Shift]','[Ctrl]','[Alt]','[Pause]','[CapsLock]');
		for (i=11;i;--i) keyDownChars.push('undefined');
		keyDownChars[27] = '[Esc]';
		keyDownChars.push(' ','[PgUp]','[PgDn]','[End]','[Home]','[Left]','[Up]','[Right]','[Down]');
		for (i=7;i;--i) keyDownChars.push('undefined');
		keyDownChars[45] = '[Ins]';
		keyDownChars[46] = '[Del]';
		keyDownChars.push(['0',')'],['1','!'],['2','@'],['3','#'],['4','$'],['5','%'],['6','^'],['7','&'],['8','*'],['9','(']);
		for (i=7;i;--i) keyDownChars.push('undefined');
		keyDownChars.push('A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','[WinKey]');
		for (i=4;i;--i) keyDownChars.push('undefined');
		keyDownChars.push('0','1','2','3','4','5','6','7','8','9','*','+','undefined','-','.','/','[F1]','[F2]','[F3]','[F4]','[F5]','[F6]','[F7]','[F8]','[F9]','[F10]','[F11]','[F12]');
		for (i=62;i;--i) keyDownChars.push('undefined');
		keyDownChars[144] = '[NumLock]';
		keyDownChars[145] = '[ScrollLock]';
		keyDownChars.push([';',':'],['=','+'],[',','<'],['-','_'],['.','>'],['/','?'],['`','~']);
		for (i=26;i;--i) keyDownChars.push('undefined');
		keyDownChars.push(['[','{'],['\\','|'],[']','}'],["'",'"']);
	return keyDownChars[keyCode];
}
