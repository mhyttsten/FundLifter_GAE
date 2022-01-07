package com.pf.fl.shared.utils;

public class IndentWriter {
	
	//------------------------------------------------------------------------
	public static final void main(String[] arg) {
		IndentWriter iw = new IndentWriter();
        iw.setIndentChar(' ');
        iw.setFlowChar('|');
		iw.println("Hello");
		iw.push();
		iw.println("Indent 1");
		iw.println("Indent 1");
		iw.push();
		iw.println("Indent 2");
		iw.println("Indent 2");
		iw.pop();
		iw.println("Indent 1 again");
		iw.println("Indent 1 again");
		iw.pop();
		iw.println("back... to start");
		System.out.println(iw.toString());
	}
	
	//------------------------------------------------------------------------
	public void setIndentChar(char c) {
		_indentChar = c;
	}
    public void setFlowChar(char c) { _flowChar = c; }
    public void generateHTML() { _generateHTML = true; }
	public void setNewline(String c) { _newline = c; }

	public void setIndentDistance(int distance) {
		_incIndent = distance;
	}
	
	//------------------------------------------------------------------------
	public IndentWriter(boolean autoFlush) {
		_autoFlush = autoFlush;
		_incIndent = 4;
	}
	
	//------------------------------------------------------------------------
	public IndentWriter() {
		_autoFlush = false;
		_incIndent = 4;
	}

	//------------------------------------------------------------------------
	public void push() {
        _currentIndentStr += _flowChar;

        for (int i=0; i < _incIndent-1; i++) {
            _currentIndentStr += _indentChar;
        }
	}
    
	//------------------------------------------------------------------------
	public void pop() {
        _currentIndentStr = _currentIndentStr.substring(0, _currentIndentStr.length() - _incIndent);
	}
	
	// ***********************************************************************

	//------------------------------------------------------------------------
	public void clear() {
		_buffer = new StringBuffer();
	}
	
	//------------------------------------------------------------------------
	public int length() {
		return _buffer.length();
	}
	
	//------------------------------------------------------------------------
	public String toString() {
		return getString();
	}

	//------------------------------------------------------------------------
	public String getString() {
		String returnValue = _buffer.toString();
		return returnValue;
	}

	//------------------------------------------------------------------------
	public void println(String a1) {
		if(a1 == null)
			a1 = "";
		print(a1 + _newline);
        _isFirstWriteLine = true;
	}

	//------------------------------------------------------------------------
	public void println() {
		print(_newline);
        _isFirstWriteLine = true;
	}

	//------------------------------------------------------------------------
	public void print(String a1) {
		if(a1 == null)
			return;

        if (_isFirstWrite || _isFirstWriteLine) {
            _buffer.append(_currentIndentStr);
        }
        _isFirstWrite = false;
        _isFirstWriteLine = false;
		_buffer.append(a1);

		if(_autoFlush) {
			String str = getString();
			clear();
			System.out.print(str);
		}
	}
	
	// ***********************************************************************

	public boolean _generateHTML;

	private int _incIndent = 2;
	private StringBuffer _buffer = new StringBuffer();
	private boolean _autoFlush;

    private boolean _isFirstWrite = true;
    private boolean _isFirstWriteLine = true;

    private String _currentIndentStr = "";
    private char _indentChar = ' ';
    private char _flowChar = ' ';
    private String _newline = "\n";
}
