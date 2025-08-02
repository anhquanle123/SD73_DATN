package com.project.DuAnTotNghiep.service.serviceImpl;

import com.project.DuAnTotNghiep.dto.CustomerDto.CustomerDto;
import com.project.DuAnTotNghiep.entity.Account;
import com.project.DuAnTotNghiep.entity.Customer;
import com.project.DuAnTotNghiep.entity.Role;
import com.project.DuAnTotNghiep.entity.enumClass.RoleName;
import com.project.DuAnTotNghiep.exception.ShopApiException;
import com.project.DuAnTotNghiep.repository.AccountRepository;
import com.project.DuAnTotNghiep.repository.CustomerRepository;
import com.project.DuAnTotNghiep.repository.RoleRepository;
import com.project.DuAnTotNghiep.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender; // Đảm bảo import
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Đảm bảo import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final JavaMailSender mailSender; // Sử dụng bean đã cấu hình
    private final BCryptPasswordEncoder passwordEncoder; // Sử dụng bean đã cấu hình

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, AccountRepository accountRepository,
                               RoleRepository roleRepository, JavaMailSender mailSender, BCryptPasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<CustomerDto> getAllCustomers(Pageable pageable) {
        Page<Customer> customerPage = customerRepository.findAll(pageable);
        return customerPage.map(this::convertToDto);
    }

    @Override
    @Transactional
    public CustomerDto createCustomerAdmin(CustomerDto customerDto) {
        System.out.println("Bắt đầu tạo khách hàng với dữ liệu: " + customerDto.toString());

        if (customerDto.getCode() == null || customerDto.getCode().trim().isEmpty()) {
            Customer customerCurrent = getLastCustomer();
            Long nextCode = (customerCurrent == null) ? 1L : customerCurrent.getId() + 1;
            String productCode = "KH" + String.format("%04d", nextCode);
            customerDto.setCode(productCode);
            System.out.println("Đã gán mã tự động: " + productCode);
        }

        if (customerRepository.existsByCode(customerDto.getCode())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã khách hàng đã tồn tại");
        }

        String email = customerDto.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Email không được bỏ trống");
        }

        if (customerRepository.existsByEmail(email) || accountRepository.existsByEmail(email)) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Email đã tồn tại trong hệ thống");
        }

        Customer customer = convertToEntity(customerDto);
        customer.setEmail(email);
        Customer savedCustomer = customerRepository.save(customer);
        System.out.println("Đã lưu Customer với ID: " + savedCustomer.getId());

        String password = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(password);
        System.out.println("Đang tạo Account với email: " + email + ", password: " + password);

        Account account = new Account();
        account.setCode("TK" + String.format("%04d", savedCustomer.getId() + 1));
        account.setEmail(email.trim());
        account.setPassword(encodedPassword);
        account.setCreateDate(LocalDateTime.now());
        account.setUpdateDate(LocalDateTime.now());
        account.setNonLocked(true);
        account.setCustomer(savedCustomer);

        // Gán role
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(RoleName.ROLE_USER);
                    role.setCreateDate(LocalDateTime.now());
                    role.setUpdateDate(LocalDateTime.now());
                    return roleRepository.save(role);
                });
        account.setRole(userRole);

        Account savedAccount = accountRepository.save(account);
        System.out.println("✔ Đã lưu Account với ID: " + savedAccount.getId() + ", role_id: " + userRole.getId());

        // Gửi email với mật khẩu ngẫu nhiên
        try {
            sendEmail(email, password);
            System.out.println("✔ Đã gửi email với mật khẩu đến: " + email);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi email đến: " + email + " - Chi tiết: " + e.getMessage());
            // Không ném exception để không làm gián đoạn lưu dữ liệu
        }

        return convertToDto(savedCustomer);
    }





    @Override
    public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ShopApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng với ID: " + id));

        // Kiểm tra trùng mã khách hàng với ID khác
        if (customerRepository.existsByCodeAndIdNot(customerDto.getCode(), id)) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã khách hàng đã tồn tại");
        }

        // Kiểm tra trùng số điện thoại với ID khác
        if (customerRepository.existsByPhoneNumberAndIdNot(customerDto.getPhoneNumber(), id)) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Số điện thoại khách hàng đã tồn tại");
        }

        String oldEmail = existingCustomer.getEmail(); // Lưu email cũ để kiểm tra

        existingCustomer.setCode(customerDto.getCode());
        existingCustomer.setName(customerDto.getName());
        existingCustomer.setEmail(customerDto.getEmail());
        existingCustomer.setPhoneNumber(customerDto.getPhoneNumber());
        existingCustomer.setAddress(customerDto.getAddress());

        Customer updatedCustomer = customerRepository.save(existingCustomer);

        // Nếu email thay đổi thì xóa tài khoản cũ và tạo lại tài khoản mới + gửi mật khẩu mới
        if (customerDto.getEmail() != null && !customerDto.getEmail().trim().isEmpty()
                && !customerDto.getEmail().equalsIgnoreCase(oldEmail)) {
            String password = generateRandomPassword();
            String encodedPassword = passwordEncoder.encode(password);
            accountRepository.deleteByEmail(oldEmail); // Xóa tài khoản theo email cũ
            createAccount(updatedCustomer, customerDto.getEmail(), encodedPassword);
            sendEmail(customerDto.getEmail(), password);
        }

        return convertToDto(updatedCustomer);
    }


    @Override
    public Page<CustomerDto> searchCustomerAdmin(String keyword, Pageable pageable) {
        Page<Customer> customerPage = customerRepository.searchCustomerKeyword(keyword, pageable);
        return customerPage.map(this::convertToDto);
    }

    @Override
    public CustomerDto getCustomerById(Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        return customer.map(this::convertToDto)
                .orElseThrow(() -> new ShopApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng với ID: " + id));
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ShopApiException(HttpStatus.BAD_REQUEST, "Không tìm thấy khách hàng với ID: " + id));
        customerRepository.delete(customer);
    }

    @Override
    public Customer getLastCustomer() {
        return customerRepository.findTopByOrderByIdDesc();
    }

    private CustomerDto convertToDto(Customer customer) {
        CustomerDto customerDto = new CustomerDto();
        customerDto.setId(customer.getId());
        customerDto.setCode(customer.getCode());
        customerDto.setName(customer.getName());
        customerDto.setEmail(customer.getEmail());
        customerDto.setPhoneNumber(customer.getPhoneNumber());
        customerDto.setAddress(customer.getAddress());
        return customerDto;
    }

    private Customer convertToEntity(CustomerDto customerDto) {
        Customer customer = new Customer();
        customer.setId(customerDto.getId());
        customer.setCode(customerDto.getCode());
        customer.setName(customerDto.getName());
        customer.setEmail(customerDto.getEmail());
        customer.setPhoneNumber(customerDto.getPhoneNumber());
        customer.setAddress(customerDto.getAddress());
        return customer;
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

    private void createAccount(Customer customer, String email, String encodedPassword) {
        if (email == null || email.trim().isEmpty()) {
            System.err.println("❌ Email là null hoặc rỗng, không thể tạo Account. Dữ liệu: " + email);
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Email không được bỏ trống");
        }
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            System.err.println("❌ Password là null hoặc rỗng, không thể tạo Account. Dữ liệu: " + encodedPassword);
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Password không hợp lệ");
        }

        Account account = new Account();
        account.setEmail(email.trim());
        account.setPassword(encodedPassword);
        account.setCreateDate(LocalDateTime.now());
        account.setUpdateDate(LocalDateTime.now());
        account.setNonLocked(true);
        if (customer != null) {
            account.setCustomer(customer);
            System.out.println("Gán customer_id: " + customer.getId());
        } else {
            System.err.println("❌ Customer là null, không thể gán vào Account");
            throw new ShopApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Customer không tồn tại");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(RoleName.ROLE_USER);
                    return roleRepository.save(role);
                });
        account.setRole(userRole);
        System.out.println("Gán role_id: " + userRole.getId());

        System.out.println("Đang lưu Account với email: " + email + ", customer_id: " + customer.getId() + ", role_id: " + userRole.getId());
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
            message.setSubject("Thông tin đăng nhập tài khoản khách hàng");
            message.setText("Chào bạn,\n\nTài khoản của bạn đã được tạo thành công.\n" +
                    "Email: " + toEmail + "\n" +
                    "Mật khẩu: " + password + "\n\n" +
                    "Vui lòng đổi mật khẩu sau khi đăng nhập lần đầu.\nTrân trọng,\nĐội ngũ hỗ trợ");
            mailSender.send(message);
            System.out.println("✔ Đã gửi email tới: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi email tới: " + toEmail);
            e.printStackTrace(); // In lỗi ra console để bạn biết lý do
        }
    }


    @Override
    public Account blockAccount(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ShopApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng với ID: " + id));
        Account account = accountRepository.findByCustomerId(id)
                .orElseThrow(() -> new ShopApiException(HttpStatus.NOT_FOUND, "Tài khoản không tồn tại cho khách hàng này"));
        account.setNonLocked(false);
        account.setUpdateDate(LocalDateTime.now());
        return accountRepository.save(account);
    }

    @Override
    public Account openAccount(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ShopApiException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng với ID: " + id));
        Account account = accountRepository.findByCustomerId(id)
                .orElseThrow(() -> new ShopApiException(HttpStatus.NOT_FOUND, "Tài khoản không tồn tại cho khách hàng này"));
        account.setNonLocked(true);
        account.setUpdateDate(LocalDateTime.now());
        return accountRepository.save(account);
    }

    @Override
    public Account getAccountByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ShopApiException(HttpStatus.NOT_FOUND, "Tài khoản không tồn tại cho khách hàng này"));
    }
}