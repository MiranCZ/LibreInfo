package me.miran.libreinfo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Csv {

    private final List<CsvLine> lines;
    private final Map<String, Integer> descriptorMap;

    public Csv(List<String> descriptors, List<List<String>> data) {
        this.lines = new ArrayList<>();
        for (List<String> line : data) {
            lines.add(new CsvLine(line));
        }

        descriptorMap = new HashMap<>();
        for (int i = 0; i < descriptors.size(); i++) {
            String desc = descriptors.get(i);

            // idfk what this is
            if (((int)desc.charAt(0)) == 65279) {
                desc = desc.substring(1);
            }
            descriptorMap.put(desc, i);
        }
    }

    public List<CsvLine> getLines() {
        return lines;
    }


    public class CsvLine {
        public final List<String> line;

        private CsvLine(List<String> line) {
            this.line = line;
        }


        public String getOrDefault(String name, String defaultValue) {
            String result = get(name);
            if (result == null) return defaultValue;
            return result;
        }

        public String get(String name) {
            int id = Csv.this.descriptorMap.getOrDefault(name, -1);

            if (id == -1) return null;
            if (id >= line.size()) {
                return null;
//                throw new IllegalStateException("Id for name "+name + " ("+id+") is outside of line: "+line);
            }
            return line.get(id);
        }

        public Boolean getBoolean(String name) {
            return getT(name, CsvLine::parseBool);
        }

        private static boolean parseBool(String str) {
            str = str.strip().toLowerCase();

            return str.equals("true") || str.equals("1");
        }

        public Integer getInt(String name) {
            return getT(name, Integer::parseInt);
        }

        public <T> T getT(String name, Function<String, T> mapper) {
            String value = get(name);
            if (value == null) return null;
            return mapper.apply(value);
        }

        @Override
        public String toString() {
            return "CsvLine{" +
                    "line=" + line +
                    '}';
        }
    }

}
