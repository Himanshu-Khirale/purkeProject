package com.hms.config;

import com.hms.entity.*;
import com.hms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

//@Configuration
//@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final MedicineRepository medicineRepository;
    private final LabOrderRepository labOrderRepository;
    private final WardRepository wardRepository;
    private final BedRepository bedRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (tenantRepository.count() > 0) {
            log.info("Database already seeded, skipping...");
            return;
        }

        log.info("Starting fresh data seeding for local profile...");

        // 1. Create Tenant
        Tenant tenant = new Tenant();
        tenant.setName("Himanshu Hospital");
        tenant.setSlug("himanshu");
        tenant.setSubscriptionPlan("premium");
        tenant.setIsActive(true);
        tenant = tenantRepository.save(tenant);
        UUID tenantId = tenant.getId();

        log.info("Seeded Tenant: id={}, slug={}", tenantId, tenant.getSlug());

        // 2. Create Roles
        Role adminRole = new Role();
        adminRole.setName("Administrator");
        adminRole.setSlug("admin");
        adminRole.setIsSystem(true);
        adminRole.setTenantId(tenantId);
        adminRole = roleRepository.save(adminRole);

        log.info("Seeded Role: id={}, name={}", adminRole.getId(), adminRole.getName());

        // 2.5 Seed Permissions for Admin
        String[][] perms = {
            {"patients", "view"}, {"patients", "create"}, {"patients", "update"}, {"patients", "delete"},
            {"doctors", "view"}, {"doctors", "create"}, {"doctors", "update"}, {"doctors", "delete"},
            {"appointments", "view"}, {"appointments", "create"}, {"appointments", "update"}, {"appointments", "cancel"},
            {"emr", "view"}, {"emr", "create"}, {"billing", "view"}, {"pharmacy", "view"}, {"lab", "view"},
            {"reports", "view_dashboard"}
        };
        for (String[] p : perms) {
            Permission permission = new Permission();
            permission.setModule(p[0]);
            permission.setAction(p[1]);
            permission.setCreatedAt(java.time.OffsetDateTime.now());
            permission = permissionRepository.save(permission);
            adminRole.getPermissions().add(permission);
        }
        roleRepository.save(adminRole);

        // 3. Create Users
        User admin = new User();
        admin.setEmail("admin@hms.com");
        admin.setPasswordHash(passwordEncoder.encode("password"));
        admin.setFirstName("System");
        admin.setLastName("Admin");
        admin.setIsActive(true);
        admin.setIsLocked(false);
        admin.setFailedLoginAttempts(0);
        admin.setTenantId(tenantId);
        admin.setRoles(Set.of(adminRole));
        userRepository.save(admin);

        User himanshu = new User();
        himanshu.setEmail("himanshu@gmail.com");
        himanshu.setPasswordHash(passwordEncoder.encode("himanshu@1234"));
        himanshu.setFirstName("Himanshu");
        himanshu.setLastName("User");
        himanshu.setIsActive(true);
        himanshu.setIsLocked(false);
        himanshu.setFailedLoginAttempts(0);
        himanshu.setTenantId(tenantId);
        himanshu.setRoles(Set.of(adminRole));
        userRepository.save(himanshu);

        log.info("Seeded Users: admin@hms.com, himanshu@gmail.com");

        // 4. Create Departments
        Department cardiology = new Department();
        cardiology.setName("Cardiology");
        cardiology.setCode("CARD");
        cardiology.setIsActive(true);
        cardiology.setTenantId(tenantId);
        cardiology = departmentRepository.save(cardiology);

        Department neurology = new Department();
        neurology.setName("Neurology");
        neurology.setCode("NEUR");
        neurology.setIsActive(true);
        neurology.setTenantId(tenantId);
        departmentRepository.save(neurology);

        // 5. Create Doctors
        Doctor drSmith = new Doctor();
        drSmith.setUserId(himanshu.getId());
        drSmith.setSpecialization("Cardiology");
        drSmith.setQualification("MD, DM");
        drSmith.setLicenseNumber("LIC-12345");
        drSmith.setExperienceYears(15);
        drSmith.setConsultationFee(new java.math.BigDecimal("1000.00"));
        drSmith.setIsAvailable(true);
        drSmith.setTenantId(tenantId);
        drSmith.setDepartmentId(cardiology.getId());
        drSmith = doctorRepository.save(drSmith);

        // 6. Create Patients
        Patient patient1 = new Patient();
        patient1.setFirstName("John");
        patient1.setLastName("Doe");
        patient1.setEmail("john.doe@example.com");
        patient1.setPhone("9876543210");
        patient1.setGender("Male");
        patient1.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));
        patient1.setMrn("MRN-2026-001");
        patient1.setTenantId(tenantId);
        patient1 = patientRepository.save(patient1);

        Patient patient2 = new Patient();
        patient2.setFirstName("Jane");
        patient2.setLastName("Smith");
        patient2.setEmail("jane.smith@example.com");
        patient2.setPhone("9876543211");
        patient2.setGender("Female");
        patient2.setDateOfBirth(java.time.LocalDate.of(1995, 5, 20));
        patient2.setMrn("MRN-2026-002");
        patient2.setTenantId(tenantId);
        patient2 = patientRepository.save(patient2);

        // 7. Create Appointments
        Appointment appt = new Appointment();
        appt.setPatientId(patient1.getId());
        appt.setDoctorId(drSmith.getId());
        appt.setAppointmentDate(java.time.LocalDate.now().plusDays(1));
        appt.setStartTime(java.time.LocalTime.of(10, 0));
        appt.setEndTime(java.time.LocalTime.of(10, 30));
        appt.setStatus("scheduled");
        appt.setReason("Routine Checkup");
        appt.setTenantId(tenantId);
        appointmentRepository.save(appt);

        // 8. Create Invoices
        Invoice inv = new Invoice();
        inv.setPatientId(patient1.getId());
        inv.setInvoiceNumber("INV-2026-001");
        inv.setInvoiceDate(java.time.LocalDate.now());
        inv.setTotalAmount(new java.math.BigDecimal("1500.00"));
        inv.setPaidAmount(new java.math.BigDecimal("1500.00"));
        inv.setSubtotal(new java.math.BigDecimal("1500.00"));
        inv.setStatus("paid");
        inv.setDueDate(java.time.LocalDate.now());
        inv.setTenantId(tenantId);
        invoiceRepository.save(inv);

        // 9. Create Medicines
        Medicine med1 = new Medicine();
        med1.setName("Amlodipine 5mg");
        med1.setCategory("Antihypertensive");
        med1.setManufacturer("Cipla");
        med1.setStockQuantity(5240);
        med1.setReorderLevel(100);
        med1.setPrice(new java.math.BigDecimal("3.50"));
        med1.setExpiryDate(java.time.LocalDate.of(2027, 12, 1));
        med1.setUnit("Tablet");
        med1.setTenantId(tenantId);
        medicineRepository.save(med1);

        Medicine med2 = new Medicine();
        med2.setName("Metformin 500mg");
        med2.setCategory("Antidiabetic");
        med2.setManufacturer("Sun Pharma");
        med2.setStockQuantity(45);
        med2.setReorderLevel(50);
        med2.setPrice(new java.math.BigDecimal("2.80"));
        med2.setExpiryDate(java.time.LocalDate.of(2027, 3, 1));
        med2.setUnit("Tablet");
        med2.setTenantId(tenantId);
        medicineRepository.save(med2);

        // 10. Create Lab Orders
        LabOrder lab1 = new LabOrder();
        lab1.setOrderNumber("LAB-0891");
        lab1.setPatient(patient1);
        lab1.setDoctor(drSmith);
        lab1.setTestName("CBC");
        lab1.setCategory("Hematology");
        lab1.setPriority("NORMAL");
        lab1.setStatus("COMPLETED");
        lab1.setResult("Hb: 14.2 g/dL, WBC: 7.5k");
        lab1.setResultDate(java.time.LocalDateTime.now());
        lab1.setTenantId(tenantId);
        labOrderRepository.save(lab1);

        // 11. Create Wards and Beds
        Ward generalWard = new Ward();
        generalWard.setName("Ward A — General");
        generalWard.setType("GENERAL");
        generalWard.setTotalBeds(10);
        generalWard.setTenantId(tenantId);
        generalWard = wardRepository.save(generalWard);

        for (int i = 1; i <= 10; i++) {
            Bed bed = new Bed();
            bed.setBedNumber("A-" + String.format("%02d", i));
            bed.setWard(generalWard);
            bed.setTenantId(tenantId);
            if (i == 1) {
                bed.setStatus("OCCUPIED");
                bed.setPatient(patient1);
            } else if (i == 6) {
                bed.setStatus("RESERVED");
            } else if (i == 9) {
                bed.setStatus("MAINTENANCE");
            } else {
                bed.setStatus("AVAILABLE");
            }
            bedRepository.save(bed);
        }

        System.out.println("============================================");
        System.out.println("  LOCAL DATA SEEDED SUCCESSFULLY");
        System.out.println("  Login 1: admin@hms.com / password");
        System.out.println("  Login 2: himanshu@gmail.com / himanshu@1234");
        System.out.println("  Tenant slug: himanshu");
        System.out.println("  Initial Data: 2 Patients, 1 Doctor, 1 Appointment, 1 Invoice");
        System.out.println("============================================");
    }
}
