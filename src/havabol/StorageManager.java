package havabol;
import java.util.ArrayList;

public class StorageManager {
    // size
    // -1 means unbounded, -100 = unknown
    public boolean isBounded;
    public boolean sizeUnknown = false;
    public int size;
    public int type;
    public ArrayList<String> val;
    public Parser parser;
    public String defaultVal = null;

    public StorageManager(Parser parser, int size, int type) {
        val = new ArrayList<String>();
        this.size = size;
        this.type = type;
        this.parser = parser;
        if (size == -1) {
            this.isBounded = false;
            this.val.add(null);
            this.size = this.val.size();
        } else if (size == -100)  { //  size will need to be set!!!!
            this.isBounded = true;
            this.sizeUnknown = true;
        } else if (size > 0) {
            this.isBounded = true;
        }
    }

    @Override
	public String toString() {
		return "StorageManager [size=" + size + ", type=" + type + ", val=" + val + ", defaultVal=" + defaultVal + "]";
	}

	public void init(int size, int type) {
        if (size > 0) {  // normal size
            for (int i=0; i < size; i++)
                this.val.add(this.defaultVal);
        }
    }

    public void add(int index, ResultValue resVal, String token) throws Exception {
        String item = resVal.value;
        if (this.type != resVal.type) {
            if (resVal.type == Token.BOOLEAN) {
                if (this.type != Token.STRING) {
                    this.parser.error("Type Bool can ONLY be casted to type STRING");
                }
            }
            if (this.type == Token.BOOLEAN && resVal.type == Token.STRING) {
                if (resVal.value.equals("T") || resVal.value.equals("F")) {;}
            } else {
                Numeric num = new Numeric(this.parser, resVal, token, "Index "+String.valueOf(index));
                if (this.type == Token.INTEGER) {
                    item = String.valueOf(num.integerValue);
                } else if (this.type == Token.FLOAT) {
                    item = String.valueOf(num.doubleValue);
                } else if (this.type == Token.STRING) {
                    item = num.strValue;
                }
            }
        }
        this.val.add(index, item);
        this.size = this.val.size();


    }

    public void set(int index, ResultValue resVal, String token) throws Exception {
        int offset;
        String item = resVal.value;
        if (this.type != resVal.type) {
            if (resVal.type == Token.BOOLEAN) {
                if (this.type != Token.STRING) {
                    this.parser.error("Type Bool can ONLY be casted to type STRING");
                }
            } else if (this.type == Token.BOOLEAN && resVal.type == Token.STRING) {
                if (resVal.value.equals("T") || resVal.value.equals("F")) {;}
            } else {
                Numeric num = new Numeric(this.parser, resVal, token, "Index "+String.valueOf(index));
                if (this.type == Token.INTEGER) {
                    item = String.valueOf(num.integerValue);
                } else if (this.type == Token.FLOAT) {
                    item = String.valueOf(num.doubleValue);
                } else if (this.type == Token.STRING) {
                    item = num.strValue;
                }
            }
        }
        try {
            if (index == -1)
                this.val.set(this.size -1, item);
            else
                this.val.set(index, item);
        } catch (Exception e) {
            if (this.isBounded) {
                this.parser.error("Out of bounds exception");
            } else {
                offset = index - this.size;
                for (int i=0; i < offset; i++)
                    this.val.add(this.defaultVal); // setting to defualt value
                this.val.add(item);
            }
        }
        this.size = this.val.size();
    }

    public ResultValue get(int index) throws Exception {
        ResultValue retVal = new ResultValue("");
        retVal.type = this.type;
        int offset;
        try {
            retVal.value = this.val.get(index);
        } catch (Exception e) {
            if (this.isBounded) {
                this.parser.error("Out of bounds exception");
            } else {
                offset = index - this.size + 1;
                for (int i = 0; i < offset; i++) {
                    this.val.add(this.defaultVal);
                }
                retVal.value = this.val.get(index);
            }
        }
        return retVal;
    }

    public void copy(StorageManager passedArray, String arrayName) throws Exception {
        arrayName = "'"+arrayName+"'";
        int passedSize = passedArray.size;
        int n = 0;
        if (passedSize > this.size)
            n = this.size;
        else
            n = passedSize;
        if (this.isBounded == false) { // array is not bounded
            for (int i=0; i < n; i++) {
                String val = passedArray.val.get(i);
                if (this.type != passedArray.type) {
                    if (passedArray.val.get(i) == null) {
                        this.val.add(i, val);
                        continue;
                    }
                    if (passedArray.type == Token.BOOLEAN) {
                        if (this.type != Token.STRING) {
                            this.parser.error("Type Bool can ONLY be casted to type STRING");
                        }
                        this.val.add(i, val);
                    } else if (this.type == Token.BOOLEAN && passedArray.type == Token.STRING) {
                        if (val.equals("T") || val.equals("F")) {
                            this.val.add(i, val);
                        }
                    } else {
                        ResultValue resVal = new ResultValue(val);
                        Numeric num = new Numeric(this.parser, resVal, arrayName, "");
                        if (this.type == Token.INTEGER) {
                            resVal.value = String.valueOf(num.integerValue);
                        } else if (this.type == Token.FLOAT) {
                            resVal.value = String.valueOf(num.doubleValue);
                        } else if (this.type == Token.STRING) {
                            resVal.value = num.strValue;
                        }
                        val = resVal.value;
                        this.val.add(i, val);
                    }
                } else { // they are same type
                    this.val.add(i, val);
                }
            }
        } else { // bounded arrays
            for (int i = 0; i < n; i++) {
                String val = passedArray.val.get(i);
                if (this.type != passedArray.type) {
                    if (passedArray.val.get(i) == null) {
                        this.val.add(i, val);
                        continue;
                    }
                    if (passedArray.type == Token.BOOLEAN) {
                        if (this.type != Token.STRING) {
                            this.parser.error("Type Bool can ONLY be casted to type STRING");
                        }
                        this.val.add(i, val);
                    } else if (this.type == Token.BOOLEAN && passedArray.type == Token.STRING) {
                        if (val.equals("T") || val.equals("F")) {
                            this.val.add(i, val);
                        }
                    } else {
                        ResultValue resVal = new ResultValue(val);
                        Numeric num = new Numeric(this.parser, resVal, arrayName, "");
                        if (this.type == Token.INTEGER) {
                            resVal.value = String.valueOf(num.integerValue);
                        } else if (this.type == Token.FLOAT) {
                            resVal.value = String.valueOf(num.doubleValue);
                        } else if (this.type == Token.STRING) {
                            resVal.value = num.strValue;
                        }
                        val = resVal.value;
                        this.val.add(i, val);
                    }
                } else {  //types are the same
                    this.val.add(i, val);
                }
            }
        }
    }

    public void defaultArray(ResultValue resVal, String token) throws Exception {
        if (this.type != resVal.type) {
            if (resVal.type == Token.BOOLEAN) {
                if (this.type != Token.STRING) {
                    this.parser.error("Type Bool can ONLY be casted to type STRING");
                } else {
                    Numeric num = new Numeric(this.parser, resVal, token, "The default value");
                    if (this.type == Token.INTEGER) {
                        resVal.value = String.valueOf(num.integerValue);
                    } else if (this.type == Token.FLOAT) {
                        resVal.value = String.valueOf(num.doubleValue);
                    } else if (this.type == Token.STRING) {
                        resVal.value = num.strValue;
                    }
                }
            }
        }
        this.defaultVal = resVal.value;
        for (int i = 0; i < this.val.size(); i++)
            this.val.set(i, this.defaultVal);
    }

}
