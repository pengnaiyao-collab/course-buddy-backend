package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.TeamDTO;
import com.coursebuddy.domain.dto.TeamMemberDTO;
import com.coursebuddy.domain.vo.TeamMemberVO;
import com.coursebuddy.domain.vo.TeamVO;
import com.coursebuddy.service.ITeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Teams", description = "Team management endpoints")
@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final ITeamService service;

    @Operation(summary = "Create a team", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TeamVO> createTeam(@Valid @RequestBody TeamDTO dto) {
        return ApiResponse.success("Team created", service.createTeam(dto));
    }

    @Operation(summary = "List my teams", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ApiResponse<Page<TeamVO>> listMyTeams(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.listMyTeams(pageable));
    }

    @Operation(summary = "Get team by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ApiResponse<TeamVO> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getTeamById(id));
    }

    @Operation(summary = "Update a team", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ApiResponse<TeamVO> update(@PathVariable Long id, @Valid @RequestBody TeamDTO dto) {
        return ApiResponse.success(service.updateTeam(id, dto));
    }

    @Operation(summary = "Delete a team", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.deleteTeam(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "Add a member to a team", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TeamMemberVO> addMember(
            @PathVariable Long id,
            @Valid @RequestBody TeamMemberDTO dto) {
        return ApiResponse.success("Member added", service.addMember(id, dto));
    }

    @Operation(summary = "Remove a member from a team", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}/members/{userId}")
    public ApiResponse<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        service.removeMember(id, userId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "Update a member's role", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/members/{userId}/role")
    public ApiResponse<TeamMemberVO> updateMemberRole(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestParam String role) {
        return ApiResponse.success(service.updateMemberRole(id, userId, role));
    }

    @Operation(summary = "List team members", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}/members")
    public ApiResponse<List<TeamMemberVO>> listMembers(@PathVariable Long id) {
        return ApiResponse.success(service.listMembers(id));
    }
}
