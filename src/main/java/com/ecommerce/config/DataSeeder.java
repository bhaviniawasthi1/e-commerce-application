package com.ecommerce.config;

import com.ecommerce.entity.*;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded — skipping");
            return;
        }

        log.info("Seeding database with demo data...");

        seedUsers();
        seedCategoriesAndProducts();

        log.info("Database seeding complete!");
    }

    private void seedUsers() {
        User admin = User.builder()
                .username("admin")
                .email("admin@velora.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("Velora")
                .role(Role.ADMIN)
                .build();

        User customer = User.builder()
                .username("customer")
                .email("customer@velora.com")
                .password(passwordEncoder.encode("customer123"))
                .firstName("John")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .build();

        userRepository.saveAll(List.of(admin, customer));
        log.info("Seeded {} users", 2);
    }

    private void seedCategoriesAndProducts() {
        Category electronics = saveCategory("Electronics",
                "Gadgets, devices, and tech accessories for everyday life");
        Category clothing = saveCategory("Clothing",
                "Trendy apparel and fashion wear for men and women");
        Category homeKitchen = saveCategory("Home & Kitchen",
                "Everything you need to furnish and equip your home");
        Category books = saveCategory("Books",
                "Bestsellers, fiction, non-fiction, and educational titles");
        Category sports = saveCategory("Sports & Outdoors",
                "Gear and equipment for sports, fitness, and outdoor adventures");

        saveProduct("Wireless Bluetooth Headphones",
                "Premium noise-cancelling headphones with 30-hour battery life and crystal-clear audio",
                electronics, new BigDecimal("79.99"), 50, "https://picsum.photos/seed/headphones/400/400");

        saveProduct("Smartphone Stand",
                "Adjustable aluminum stand compatible with all phones and tablets",
                electronics, new BigDecimal("24.99"), 120, "https://picsum.photos/seed/stand/400/400");

        saveProduct("USB-C Hub 7-in-1",
                "Multi-port adapter with HDMI, USB 3.0, SD card reader, and PD charging",
                electronics, new BigDecimal("34.99"), 80, "https://picsum.photos/seed/usbhub/400/400");

        saveProduct("Mechanical Keyboard",
                "RGB backlit mechanical keyboard with Cherry MX switches",
                electronics, new BigDecimal("89.99"), 35, "https://picsum.photos/seed/keyboard/400/400");

        saveProduct("Cotton T-Shirt",
                "Soft 100% organic cotton t-shirt available in multiple colors",
                clothing, new BigDecimal("19.99"), 200, "https://picsum.photos/seed/tshirt/400/400");

        saveProduct("Denim Jacket",
                "Classic denim jacket with a modern fit — perfect for layering",
                clothing, new BigDecimal("59.99"), 45, "https://picsum.photos/seed/jacket/400/400");

        saveProduct("Running Shoes",
                "Lightweight cushioned running shoes with breathable mesh upper",
                clothing, new BigDecimal("94.99"), 60, "https://picsum.photos/seed/shoes/400/400");

        saveProduct("Leather Wallet",
                "Genuine leather bifold wallet with RFID blocking technology",
                clothing, new BigDecimal("39.99"), 90, "https://picsum.photos/seed/wallet/400/400");

        saveProduct("Stainless Steel Water Bottle",
                "Double-wall insulated bottle — keeps drinks cold 24h or hot 12h",
                homeKitchen, new BigDecimal("22.99"), 150, "https://picsum.photos/seed/bottle/400/400");

        saveProduct("Non-Stick Cooking Pan Set",
                "3-piece granite non-stick pan set with ergonomic handles",
                homeKitchen, new BigDecimal("49.99"), 40, "https://picsum.photos/seed/pans/400/400");

        saveProduct("LED Desk Lamp",
                "Touch-controlled desk lamp with 5 brightness levels and USB charging port",
                homeKitchen, new BigDecimal("29.99"), 70, "https://picsum.photos/seed/lamp/400/400");

        saveProduct("Scented Candle Set",
                "Set of 4 hand-poured soy wax candles — vanilla, lavender, eucalyptus, rose",
                homeKitchen, new BigDecimal("18.99"), 100, "https://picsum.photos/seed/candles/400/400");

        saveProduct("The Art of Programming",
                "A comprehensive guide to writing clean, maintainable code",
                books, new BigDecimal("29.99"), 0, "https://picsum.photos/seed/programming/400/400");

        saveProduct("Mystery Novel Collection",
                "Bestselling thriller trilogy in a collectible box set",
                books, new BigDecimal("34.99"), 25, "https://picsum.photos/seed/novel/400/400");

        saveProduct("Yoga Mat",
                "Extra-thick non-slip yoga mat with carrying strap, 6mm厚度",
                sports, new BigDecimal("25.99"), 85, "https://picsum.photos/seed/yoga/400/400");

        saveProduct("Resistance Bands Set",
                "5-level resistance band set for home workouts and rehabilitation",
                sports, new BigDecimal("14.99"), 110, "https://picsum.photos/seed/bands/400/400");

        saveProduct("Camping Tent 4-Person",
                "Waterproof 4-person dome tent with easy setup — includes carry bag",
                sports, new BigDecimal("129.99"), 15, "https://picsum.photos/seed/tent/400/400");

        log.info("Seeded {} categories and {} products", 5, 17);
    }

    private Category saveCategory(String name, String description) {
        Category category = Category.builder()
                .name(name)
                .description(description)
                .imageUrl("https://picsum.photos/seed/" + name.toLowerCase().replaceAll("\\s+", "") + "/400/300")
                .build();
        return categoryRepository.save(category);
    }

    private void saveProduct(String name, String description, Category category,
                             BigDecimal price, int stock, String imageUrl) {
        Product product = Product.builder()
                .name(name)
                .description(description)
                .category(category)
                .price(price)
                .stock(stock)
                .imageUrl(imageUrl)
                .status(stock == 0 ? ProductStatus.OUT_OF_STOCK : ProductStatus.ACTIVE)
                .build();
        productRepository.save(product);
    }
}
