package Bedrest;

public class ActivityEntry
{
    public String owner;
    public String type;
    public String name;
    public int start;
    public int duration;
    
    public ActivityEntry(String o,String t, String n, int s, int d)
    {
        owner = o;
        type = t;
        name = n;
        start = s;
        duration = d;
    }
    
    public String toNddl()
    {
        StringBuffer buf = new StringBuffer();
        
        buf.append("goal(").append(owner).append(".").append(type).append(" ").append(name).append(");\n")
           .append("eq(").append(name).append(".start,").append(start).append(");\n")
           .append("eq(").append(name).append(".duration,").append(duration).append(");\n")
        ;   
        return buf.toString();
        
    }
}