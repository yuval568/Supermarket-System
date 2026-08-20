package employees.repository;

import employees.domain.Branch;
import employees.dto.BranchDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BranchRepository {

    private final Connection connection;

    public BranchRepository() throws SQLException {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public void insert(Branch branch) throws SQLException {
        String sql = "INSERT INTO branches (branch_id, name, address) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, branch.getBranchId());
            stmt.setString(2, branch.getName());
            stmt.setString(3, branch.getAddress());
            stmt.executeUpdate();
        }
    }

    public List<Branch> findAll() throws SQLException {
        List<Branch> branches = new ArrayList<>();
        String sql = "SELECT * FROM branches";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                BranchDTO dto = mapRowToDTO(rs);
                Branch branch = dtoToBranch(dto);
                if (branch != null) branches.add(branch);
            }
        }
        return branches;
    }

    public Branch findById(String branchId) throws SQLException {
        String sql = "SELECT * FROM branches WHERE branch_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BranchDTO dto = mapRowToDTO(rs);
                    return dtoToBranch(dto);
                }
            }
        }
        return null;
    }

    public void update(Branch branch) throws SQLException {
        String sql = "UPDATE branches SET name = ?, address = ? WHERE branch_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, branch.getName());
            stmt.setString(2, branch.getAddress());
            stmt.setString(3, branch.getBranchId());
            stmt.executeUpdate();
        }
    }

    public boolean exists(String branchId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM branches WHERE branch_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Maps a database row to a BranchDTO
    private BranchDTO mapRowToDTO(ResultSet rs) throws SQLException {
        BranchDTO dto = new BranchDTO();
        dto.branchId = rs.getString("branch_id");
        dto.name = rs.getString("name");
        dto.address = rs.getString("address");
        return dto;
    }

    // Converts a BranchDTO to a Branch domain object
    private Branch dtoToBranch(BranchDTO dto) {
        try {
            return new Branch(dto.branchId, dto.name, dto.address);
        } catch (Exception e) {
            System.out.println("Error converting DTO to Branch: " + e.getMessage());
            return null;
        }
    }
}