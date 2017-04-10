package havabol;
import java.util.ArrayList;

public class StorageManager {
    // size
    // -1 means unbounded, -100 = unknown
    public boolean isBounded;
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
            this.size = this.val.size();
        } else if (size == -100 || size > 0) { //  size will need to be set!!!!
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

    public void add(int index, ResultValue resVal) throws Exception {
        String item = resVal.value;
        int ind;
        System.out.println("this type: "+this.type+"\nrevVal type: "+resVal.type);
        if (this.type != resVal.type) {
            if (this.type == Token.INTEGER && resVal.type == Token.FLOAT) {
                ind = item.indexOf(".");
                item = item.substring(0, ind);
            } else if (this.type == Token.FLOAT && resVal.type == Token.INTEGER) {
                item += ".00";
            } else {
                this.parser.error("Incompatible array value type");
            }
        }

        this.val.add(index, item);

    }

    public void set(int index, ResultValue resVal) throws Exception {
        int offset;
        String item = resVal.value;
        if (this.type != resVal.type) {
            this.parser.error("Incompatible array value type");
        }

        try {
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

    public void copy(StorageManager passedArray) {
        int passedSize = passedArray.size;
        int offset = 0;
        int n = 0;
        if (passedSize > this.size)
            n = this.size;
        else
            n = passedSize;
        if (this.isBounded == false) { // array is not bounded
            offset = passedSize - size;
            for (int i=0; i < offset; i++)
                this.val.add(this.defaultVal);
        }
        for (int i = 0; i < n; i++)
            this.val.add(i, passedArray.val.get(i));
    }

    public void defaultArray(String defaultVal) throws Exception {
        this.defaultVal = defaultVal;
        for (int i = 0; i < this.val.size(); i++)
            this.val.set(i, this.defaultVal);

    }

}
