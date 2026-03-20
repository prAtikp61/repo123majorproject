// package com.Major.majorProject.entity;

// import jakarta.persistence.*;
// import lombok.*;

// import java.util.List;


// @Entity
// @Getter @Setter
// @NoArgsConstructor @AllArgsConstructor
// public class CafeOwner {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;
    
//     // In CafeOwner.java
//     private String stripeAccountId;

//     private String name;

//     @Column(unique = true, nullable = false)
//     private String email;

//     private String phone;

//     private String password;  //Bcrypt hashing

//     @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
//     private List<Cafe> cafes;
// }


package com.Major.majorProject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity

public class CafeOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Stores the Stripe Connect account ID (e.g., acct_xxxxxxxxxxxxxx) for this owner.
     * This is required to process payments and transfers to the owner.
     */
    private String stripeAccountId;

    // This field can co-exist if you use multiple payment providers.
    private String razorpayAccountId;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    private String password;  // Bcrypt hashing

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cafe> cafes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStripeAccountId() {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId) {
        this.stripeAccountId = stripeAccountId;
    }

    public String getRazorpayAccountId() {
        return razorpayAccountId;
    }

    public void setRazorpayAccountId(String razorpayAccountId) {
        this.razorpayAccountId = razorpayAccountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Cafe> getCafes() {
        return cafes;
    }

    public void setCafes(List<Cafe> cafes) {
        this.cafes = cafes;
    }
}