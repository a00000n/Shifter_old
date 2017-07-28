package alon.com.shifter.utils_shift;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import alon.com.shifter.base_classes.Consts;

/**
 * Created by Alon on 11/11/2016.
 */
public final class SpecSettings implements Serializable {

    private String[] mHeaders;
    private HashMap<Integer, ArrayList<String>> mChildren;


    private SpecSettings() {
    }

    public static SpecSettings generateFromList(List<? extends String> items) {
        SpecSettings mInstance = new SpecSettings();
        mInstance.mHeaders = new String[items.size()];
        mInstance.mChildren = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            String str = items.get(i);
            if (str.contains("=")) {
                String[] subParts = str.split("=");
                mInstance.mHeaders[i] = subParts[0];
                ArrayList<String> children = new ArrayList<>();
                Collections.addAll(children, subParts[1].split("~"));
                mInstance.mChildren.put(i, children);
            } else
                mInstance.mHeaders[i] = str;
        }
        return mInstance;
    }

    public static SpecSettings fromString(String json) throws JSONException {
        if (json.equals(Consts.Strings.NULL))
            return getEmpty();
        ArrayList<String> headers = new ArrayList<>();
        HashMap<Integer, ArrayList<String>> childMap = new HashMap<>();

        SpecSettings instance = new SpecSettings();
        JSONObject mObject = new JSONObject(json);
        Iterator<String> mIter = mObject.keys();
        int iter = 0;
        while (mIter.hasNext()) {
            String header = mIter.next();
            try {
                JSONArray arr = mObject.getJSONArray(header);
                //If there is an exception the line above is the only line to run here.
                ArrayList<String> mList = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++)
                    mList.add((String) arr.get(i));
                childMap.put(iter, mList);
            } catch (JSONException ex) {
                headers.add(header);
            }
            headers.add(header);
            iter++;
        }
        instance.mHeaders = Arrays.copyOf(headers.toArray(), headers.size(), String[].class);
        instance.mChildren = childMap;
        return instance;
    }

    public static SpecSettings getEmpty() {
        return new SpecSettings();
    }

    public boolean containsHeader(String setting) {
        for (String str : mHeaders)
            if (str.equals(setting))
                return true;
        return false;
    }

    public ArrayList<String> getChildrenArrayList(String setting) {
        for (int i = 0; i < mHeaders.length; i++) {
            String str = mHeaders[i];
            if (str.equals(setting))
                return mChildren.get(i);
        }
        return null;
    }

    @Override
    public String toString() {
        if (!isEmpty()) {
            JSONObject mJson = new JSONObject();
            try {
                for (String mHeader : mHeaders) {
                    ArrayList<String> mChildList = getChildrenArrayList(mHeader);
                    if (mChildList != null) {
                        JSONArray mArr = new JSONArray();
                        for (String mChild : mChildList)
                            mArr.put(mChild);
                        mJson.put(mHeader, mArr);
                    } else
                        mJson.put(mHeader, 0);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return mJson.toString();
        } else
            return "{}";
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SpecSettings) {
            boolean isEmptyThis = isEmpty();
            boolean isEmptyOther = ((SpecSettings) other).isEmpty();
            if (isEmptyThis == isEmptyOther) {
                if (!isEmptyOther) {
                    String[] mHeadersOther = ((SpecSettings) other).mHeaders;
                    HashMap<Integer, ArrayList<String>> mChildrenOther = ((SpecSettings) other).mChildren;
                    if (Arrays.equals(mHeaders, mHeadersOther)) {
                        for (Integer mChildKey : mChildrenOther.keySet()) {
                            ArrayList<String> mOtherChildren = mChildrenOther.get(mChildKey);
                            ArrayList<String> mThisChildren = mChildren.get(mChildKey);
                            if (mOtherChildren == null && mThisChildren == null)
                                continue;
                            if (mOtherChildren != null)
                                if (!mOtherChildren.equals(mThisChildren))
                                    return false;
                        }
                        return true;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return mHeaders == null && mChildren == null;
    }
}
