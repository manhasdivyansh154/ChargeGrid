package chargegrid.model;

public class Technician {
    private final int id;
    private final String name;
    private final String phone;
    private final String speciality;

    public Technician(int id, String name, String phone, String speciality) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.speciality = speciality;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getSpeciality() { return speciality; }
}
