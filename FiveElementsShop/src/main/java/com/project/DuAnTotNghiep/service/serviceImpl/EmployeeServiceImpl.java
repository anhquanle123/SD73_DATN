package com.project.DuAnTotNghiep.service.serviceImpl;

import com.project.DuAnTotNghiep.dto.Employee.EmployeeDto;
import com.project.DuAnTotNghiep.entity.Account;
import com.project.DuAnTotNghiep.entity.Employee;
import com.project.DuAnTotNghiep.entity.Role;
import com.project.DuAnTotNghiep.entity.enumClass.RoleName;
import com.project.DuAnTotNghiep.exception.ShopApiException;
import com.project.DuAnTotNghiep.repository.AccountRepository;
import com.project.DuAnTotNghiep.repository.EmployeeRepository;
import com.project.DuAnTotNghiep.repository.RoleRepository;
import com.project.DuAnTotNghiep.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, AccountRepository accountRepository,
                               RoleRepository roleRepository, JavaMailSender mailSender, BCryptPasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<EmployeeDto> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    @Transactional
    public EmployeeDto createEmployeeAdmin(EmployeeDto employeeDto) {
        System.out.println("Bắt đầu tạo nhân viên với dữ liệu: " + employeeDto);

        if (employeeDto.getCode() == null || employeeDto.getCode().trim().isEmpty()) {
            Employee employeeCurrent = getLastEmployee();
            Long nextCode = (employeeCurrent == null) ? 1L : employeeCurrent.getId() + 1;
            String employeeCode = "NV" + String.format("%04d", nextCode);
            employeeDto.setCode(employeeCode);
            System.out.println("Đã gán mã tự động: " + employeeCode);
        }

        if (employeeRepository.existsByCode(employeeDto.getCode())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã nhân viên đã tồn tại");
        }

        String email = employeeDto.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Email không được bỏ trống");
        }

        if (employeeRepository.existsByEmail(email) || accountRepository.existsByEmail(email)) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Email đã tồn tại trong hệ thống");
        }

        Employee employee = convertToEntity(employeeDto);
        employee.setStatus(1); // Mặc định status = 1 (Hoạt động)
        Employee savedEmployee = employeeRepository.save(employee);
        System.out.println("Đã lưu Employee với ID: " + savedEmployee.getId());

        String password = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(password);
        System.out.println("Đang tạo Account với email: " + email + ", password: " + password);

        Account account = new Account();
        account.setCode("TK" + String.format("%04d", savedEmployee.getId() + 1));
        account.setEmail(email.trim());
        account.setPassword(encodedPassword);
        account.setCreateDate(LocalDateTime.now());
        account.setUpdateDate(LocalDateTime.now());
        account.setNonLocked(true);
        account.setEmployee(savedEmployee);

        Role employeeRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(RoleName.ROLE_EMPLOYEE);
                    role.setCreateDate(LocalDateTime.now());
                    role.setUpdateDate(LocalDateTime.now());
                    return roleRepository.save(role);
                });
        account.setRole(employeeRole);

        Account savedAccount = accountRepository.save(account);
        System.out.println("✔ Đã lưu Account với ID: " + savedAccount.getId() + ", role_id: " + employeeRole.getId());

        try {
            sendEmail(email, password);
            System.out.println("✔ Đã gửi email với mật khẩu đến: " + email);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi email đến: " + email + " - Chi tiết: " + e.getMessage());
        }

        return convertToDto(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new ShopApiException(HttpStatus.NOT_FOUND, "Không tìm thấy nhân viên với ID: " + id));

        if (employeeRepository.existsByCodeAndIdNot(employeeDto.getCode(), id)) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã nhân viên đã tồn tại");
        }

        if (employeeRepository.existsByPhoneNumberAndIdNot(employeeDto.getPhoneNumber(), id)) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Số điện thoại nhân viên đã tồn tại");
        }

        String oldEmail = existingEmployee.getEmail();

        existingEmployee.setCode(employeeDto.getCode());
        existingEmployee.setName(employeeDto.getName());
        existingEmployee.setEmail(employeeDto.getEmail());
        existingEmployee.setPhoneNumber(employeeDto.getPhoneNumber());
        existingEmployee.setTinhThanh(employeeDto.getTinhThanh());
        existingEmployee.setGender(employeeDto.getGender());

        Employee updatedEmployee = employeeRepository.save(existingEmployee);

        if (employeeDto.getEmail() != null && !employeeDto.getEmail().trim().isEmpty()
                && !employeeDto.getEmail().equalsIgnoreCase(oldEmail)) {
            String password = generateRandomPassword();
            String encodedPassword = passwordEncoder.encode(password);
            accountRepository.deleteByEmail(oldEmail);
            createAccount(updatedEmployee, employeeDto.getEmail(), encodedPassword);
            sendEmail(employeeDto.getEmail(), password);
        }

        return convertToDto(updatedEmployee);
    }

    @Override
    public Page<EmployeeDto> searchEmployeeAdmin(String keyword, Pageable pageable) {
        return employeeRepository.searchEmployeeByKeyword(keyword, pageable).map(this::convertToDto);
    }

    @Override
    public EmployeeDto getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ShopApiException(HttpStatus.NOT_FOUND, "Không tìm thấy nhân viên với ID: " + id));
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ShopApiException(HttpStatus.BAD_REQUEST, "Không tìm thấy nhân viên với ID: " + id));
        employeeRepository.delete(employee);
    }

    @Override
    public Employee getLastEmployee() {
        return employeeRepository.findTopByOrderByIdDesc();
    }

    private EmployeeDto convertToDto(Employee employee) {
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setId(employee.getId());
        employeeDto.setCode(employee.getCode());
        employeeDto.setName(employee.getName());
        employeeDto.setEmail(employee.getEmail());
        employeeDto.setPhoneNumber(employee.getPhoneNumber());
        employeeDto.setTinhThanh(employee.getTinhThanh());
        employeeDto.setGender(employee.getGender());
        return employeeDto;
    }

    private Employee convertToEntity(EmployeeDto employeeDto) {
        Employee employee = new Employee();
        employee.setId(employeeDto.getId());
        employee.setCode(employeeDto.getCode());
        employee.setName(employeeDto.getName());
        employee.setEmail(employeeDto.getEmail());
        employee.setPhoneNumber(employeeDto.getPhoneNumber());
        employee.setTinhThanh(employeeDto.getTinhThanh());
        employee.setGender(employeeDto.getGender());
        return employee;
    }

    private String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }

    private void createAccount(Employee employee, String email, String encodedPassword) {
        if (email == null || email.trim().isEmpty()) {
            System.err.println("❌ Email là null hoặc rỗng, không thể tạo Account. Dữ liệu: " + email);
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Email không được bỏ trống");
        }
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            System.err.println("❌ Password là null hoặc rỗng, không thể tạo Account. Dữ liệu: " + encodedPassword);
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Password không hợp lệ");
        }

        Account account = new Account();
        account.setCode("TK" + String.format("%04d", employee.getId() + 1));
        account.setEmail(email.trim());
        account.setPassword(encodedPassword);
        account.setCreateDate(LocalDateTime.now());
        account.setUpdateDate(LocalDateTime.now());
        account.setNonLocked(true);
        if (employee != null) {
            account.setEmployee(employee);
            System.out.println("Gán employee_id: " + employee.getId()); // Sửa từ customer_id
        } else {
            System.err.println("❌ Employee là null, không thể gán vào Account");
            throw new ShopApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Employee không tồn tại");
        }

        Role employeeRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(RoleName.ROLE_EMPLOYEE);
                    role.setCreateDate(LocalDateTime.now());
                    role.setUpdateDate(LocalDateTime.now());
                    return roleRepository.save(role);
                });
        account.setRole(employeeRole);
        System.out.println("Gán role_id: " + employeeRole.getId());

        System.out.println("Đang lưu Account với email: " + email + ", employee_id: " + employee.getId() + ", role_id: " + employeeRole.getId());
        try {
            Account savedAccount = accountRepository.save(account);
            System.out.println("✔ Đã lưu Account với ID: " + savedAccount.getId());
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lưu Account: " + e.getMessage());
            e.printStackTrace();
            throw new ShopApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể tạo tài khoản: " + e.getMessage());
        }
    }

    private void sendEmail(String toEmail, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Thông tin đăng nhập tài khoản nhân viên");
            message.setText("Chào bạn,\n\nTài khoản của bạn đã được tạo thành công.\n" +
                    "Email: " + toEmail + "\n" +
                    "Mật khẩu: " + password + "\n\n" +
                    "Vui lòng đổi mật khẩu sau khi đăng nhập lần đầu.\nTrân trọng,\nĐội ngũ hỗ trợ");
            mailSender.send(message);
            System.out.println("✔ Đã gửi email tới: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi email tới: " + toEmail);
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public Account blockAccount(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ShopApiException(HttpStatus.NOT_FOUND, "Không tìm thấy nhân viên với ID: " + id));
        Account account = accountRepository.findByEmployeeId(id)
                .orElseThrow(() -> new ShopApiException(HttpStatus.NOT_FOUND, "Tài khoản không tồn tại cho nhân viên này"));
        account.setNonLocked(false);
        account.setUpdateDate(LocalDateTime.now());
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public Account openAccount(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ShopApiException(HttpStatus.NOT_FOUND, "Không tìm thấy nhân viên với ID: " + id));
        Account account = accountRepository.findByEmployeeId(id)
                .orElseThrow(() -> new ShopApiException(HttpStatus.NOT_FOUND, "Tài khoản không tồn tại cho nhân viên này"));
        account.setNonLocked(true);
        account.setUpdateDate(LocalDateTime.now());
        return accountRepository.save(account);
    }

    @Override
    public Account getAccountByEmployeeId(Long employeeId) {
        return accountRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ShopApiException(HttpStatus.NOT_FOUND, "Tài khoản không tồn tại cho nhân viên này"));
    }
}