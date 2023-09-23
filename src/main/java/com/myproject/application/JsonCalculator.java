package com.myproject.application;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.management.Query;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.math.BigDecimal.valueOf;


public class JsonCalculator {


    private static final DecimalFormat decfor = new DecimalFormat("0.00");

    public static void main(String[] args) {
        // Don't change this part
        if (args.length == 3 || args.length == 0) {
            // Path to the data file, e.g. data/data.xml
            final String DATA_FILE = args[0];
            File dataFile = new File(DATA_FILE);

            // Path to the data file, e.g. operations/operations.xml
            final String OPERATIONS_FILE = args[1];
            File operationFile = new File(OPERATIONS_FILE);

            // Path to the output file
            final String OUTPUT_FILE = args[2];
            File outputFile = new File(OUTPUT_FILE);

            List<Entry> entryList;
            List<Operation> opList;
            List<Output> outputs;
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            try
            {
                entryList = objectMapper.readValue(dataFile, InputData.class).getEntries();
                opList = objectMapper.readValue(operationFile, OperationData.class).getOperations();
                outputs = doOperations(entryList, opList);

                objectMapper.writeValue(outputFile, outputs);
            }
            catch (IOException | IllegalAccessException | InvocationTargetException e)
            {
                System.out.println("Error occured : " + e.getMessage());
            }

        } else {
            System.exit(1);
        }
    }

    static List<Output>  doOperations(List<Entry> entries, List<Operation> ops) throws InvocationTargetException, IllegalAccessException
    {
        List<Output> outputList = new ArrayList<>();
        for(Operation op : ops)
        {
            List<Entry> filteredEntry = entries.stream().filter(e -> e.getName().matches(op.getFilter())).collect(Collectors.toList());
            if(!op.getField().isEmpty())
            {
                BigDecimal result = getResult(op, filteredEntry);
                outputList.add(new Output(op.name, decfor.format(result)));
            }
        }
        return outputList;
    }

    private static BigDecimal getResult(Operation op, List<Entry> filteredEntry) throws IllegalAccessException, InvocationTargetException
    {
        int count = 0;
        BigDecimal result = new BigDecimal(0);
        for(Entry ent : filteredEntry)
        {
            Object entry = null;
            if ("sum".equals(op.getFunction()))
            {
                entry = getObjectValue(op, ent);
                result = result.add(getBigDecimalValue(entry));
            }
            else if ("min".equals(op.getFunction()))
            {
                entry = getObjectValue(op, ent);
                count++;
                if (count == 1)
                {
                    result = getBigDecimalValue(entry);
                }
                else
                {
                    result = result.min(getBigDecimalValue(entry));
                }
            }
            else if ("max".equals(op.getFunction()))
            {
                entry = getObjectValue(op, ent);
                count++;
                if (count == 1)
                {
                    result = getBigDecimalValue(entry);
                }
                else
                {
                    result = result.max(getBigDecimalValue(entry));
                }
            }
            else if ("average".equals(op.getFunction()))
            {
                entry = getObjectValue(op, ent);
                count++;
                result = result.add(getBigDecimalValue(entry));
            }
        }
        if("average".equals(op.getFunction()) && count>0)
        {
            result = result.divide(new BigDecimal(count),2, RoundingMode.HALF_UP);
        }
        return result;
    }

    private static Object getObjectValue(Operation op, Entry ent) throws IllegalAccessException, InvocationTargetException
    {
        Object obj = ent;
        Entry entry = new Entry();
        ExtendedStatistics extendedStatistics = new ExtendedStatistics();
        for (String fieldName : op.getField())
        {
            Field[] allEntryField = Entry.class.getDeclaredFields();
            Field[] allExtendedStatisticsField = ExtendedStatistics.class.getDeclaredFields();
            Optional<Field> field1 = Arrays.stream(allEntryField).filter(f ->
                f.getName().equals(fieldName)).findAny();
            if (field1.isPresent())
            {
                obj = getGetterMethodNameForField(entry, field1.get()).invoke(obj);
            }
            else
            {
                Optional<Field> field2 = Arrays.stream(allExtendedStatisticsField).filter(f ->
                    f.getName().equals(fieldName)).findAny();
                if (field2.isPresent())
                {
                    obj = getGetterMethodNameForField(extendedStatistics, field2.get()).invoke(obj);
                }
            }

        }
        return obj;
    }

    public static Method getGetterMethodNameForField(Object obj, Field field) {
        String methodName = "";
        if (field.getType() == Boolean.class
            || field.getType() == boolean.class)
        {
            methodName = "is"
                + field.getName().substring(0, 1).toUpperCase()
                + field.getName().substring(1);
        }
        else
        {
            methodName = "get" + field.getName().substring(0, 1).toUpperCase()
                + field.getName().substring(1);
        }
        try
        {
            return obj.getClass().getDeclaredMethod(methodName);
        }
        catch (NoSuchMethodException nsme)
        {
            return null;
        }
    }

    public static class Entry
    {
        String name;
        int population;
        ExtendedStatistics extendedStatistics;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public int getPopulation()
        {
            return population;
        }

        public void setPopulation(int population)
        {
            this.population = population;
        }

        public ExtendedStatistics getExtendedStatistics()
        {
            return extendedStatistics;
        }

        public void setExtendedStatistics(ExtendedStatistics extendedStatistics)
        {
            this.extendedStatistics = extendedStatistics;
        }
    }

    public static class Operation
    {
        String name;
        String function;
        ArrayList<String> field;
        String filter;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getFunction()
        {
            return function;
        }

        public void setFunction(String function)
        {
            this.function = function;
        }

        public List<String> getField()
        {
            return new ArrayList<>(this.field);
        }

        public void setField(List<String> field)
        {
            this.field = new ArrayList<>(field);
        }

        public String getFilter()
        {
            return filter;
        }

        public void setFilter(String filter)
        {
            this.filter = filter;
        }
    }

    public static class ExtendedStatistics {

        float area;

        public float getArea()
        {
            return area;
        }

        public void setArea(float area)
        {
            this.area = area;
        }
    }


    public static class Output
    {
        String name;
        String roundedValue;

        public Output()
        {
        }

        public Output(String name, String roundedValue)
        {
            this.name = name;
            this.roundedValue = roundedValue;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getRoundedValue()
        {
            return roundedValue;
        }

        public void setRoundedValue(String roundedValue)
        {
            this.roundedValue = roundedValue;
        }

        public void setRoundedValue(double roundedValue)
        {
            this.roundedValue = decfor.format(roundedValue);
        }
    }

    public static class InputData
    {
        List<Entry> entries;

        public List<Entry> getEntries()
        {
            return entries;
        }

        public void setEntries(List<Entry> entries)
        {
            this.entries = entries;
        }
    }

    public static class OperationData
    {
        ArrayList<Operation> operations;

        public List<Operation> getOperations()
        {
            return new ArrayList<>(this.operations);
        }

        public void setOperations(List<Operation> operations)
        {
            this.operations = new ArrayList<>(operations);
        }
    }

    public static BigDecimal getBigDecimalValue(Object entry)
    {
        BigDecimal f = new BigDecimal(0);
       if(entry instanceof Integer)
       {
           f = new BigDecimal((int)entry);
       }
       else if (entry instanceof Float)
       {
           f = valueOf((float)entry);
       }

        String query = "Select * from employes where id =${x}";

        PreparedStatement ps = new PreparedStatement();
        ps.setBigDecimal(ID, x);
        ps.executeQuery();

       return f;
    }

}

