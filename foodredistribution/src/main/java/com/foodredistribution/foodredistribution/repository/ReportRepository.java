package com.foodredistribution.foodredistribution.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodredistribution.foodredistribution.entity.FoodClaim;
import com.foodredistribution.foodredistribution.entity.Report;
import com.foodredistribution.foodredistribution.entity.User;
import com.foodredistribution.foodredistribution.enums.ReportReason;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByReportedUser(User reportedUser);

    List<Report> findByClaim(FoodClaim claim);

    long countByReportedUserAndReason(User user, ReportReason reason);

    List<Report> findByReviewed(boolean reviewed);
}
