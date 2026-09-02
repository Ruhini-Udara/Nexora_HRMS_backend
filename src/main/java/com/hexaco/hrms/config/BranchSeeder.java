package com.hexaco.hrms.config;

import com.hexaco.hrms.models.Branch;
import com.hexaco.hrms.repository.BranchRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class BranchSeeder {

    @Bean
    CommandLineRunner initBranches(BranchRepository branchRepository) {
        return args -> {
            if (branchRepository.count() == 0) {
                List<String> branches = Arrays.asList(
                        "Ragama", "Trincomalee", "Bambalapitiya", "Negombo", "Colombo", 
                        "Moratuwa", "Matara", "Kiribathgoda", "Jaffna", "Kandy", 
                        "Gampaha", "Ward Place", "MKI", "Kelaniya", "Peradeniya", 
                        "Ella", "Matale", "Kadawatha", "Nugegoda", "KKS", "Dehiwala"
                );

                for (String branchName : branches) {
                    if (branchRepository.findByName(branchName).isEmpty()) {
                        Branch branch = new Branch();
                        branch.setName(branchName);
                        branchRepository.save(branch);
                    }
                }
                System.out.println("Branch table seeded with default branches.");
            }
        };
    }
}
