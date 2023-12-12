package com.beatblendr.entity;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

//this class is a JPA entity to map to a database table
@Entity
@Table(name = "USER_DETAILS")
// Lombok to generate getters for all fields.
@Getter
// Lombok to generate setters for all fields.
@Setter
public class UserDetails implements Serializable{
    // serialVersionUID is a unique identifier for Serializable classes.
   private static final long serialVersionUID = 3937414011943770889L;

   //primary key of the entity.
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Integer id;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "EMAIL_ID")
    private String emailId;
 
	@Column(name = "ACCESS_TOKEN")
	private String accessToken;

	@Column(name = "REFRESH_TOKEN")
	private String refreshToken;

	@Column(name = "REF_ID")
	private String refId;

    @Column(name = "EXPIRATION_TIME_MILLIS")
    private Long expirationTimeMillis; 

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_ON")
    private Date CreatedOn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED_ON")
    private Date UpdatedOn;

}
