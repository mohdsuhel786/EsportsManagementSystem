package com.battle.serviceImpl;

import com.battle.auth.AuthenticationResponse;
import com.battle.entities.ERole;
import com.battle.entities.Role;
import com.battle.entities.TeamMember;
import com.battle.entities.Tournament;
import com.battle.entities.User;
import com.battle.exception.BattleException;
import com.battle.payloads.TeamMemberDto;
import com.battle.payloads.UserDto;
import com.battle.payloads.UserUpdateDto;
import com.battle.repositories.RoleRepository;
import com.battle.repositories.TeamMemberRepository;
import com.battle.repositories.TournamentRepository;
import com.battle.repositories.UserRepository;
import com.battle.services.JwtService;
import com.battle.services.MyUserDetails;
import com.battle.services.TeamMemberService;
import com.battle.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TeamMemberRepository teamMemberRepository;

	@Autowired
	private TournamentRepository tournamentRepository;

	@Autowired
	private TeamMemberService teamMemberService;

	@Autowired
	private JwtService jwtService;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private RoleRepository roleRepository;

	public AuthenticationResponse registerUser(UserDto user) throws BattleException {

		String email = user.getEmail();
		Optional<User> getEmail = userRepository.findByEmail(email);
		if (getEmail.isPresent()) {
			throw new BattleException("User Already Register");
		}
		User user1 = new User();
		user1.setName(user.getName());
		user1.setEmail(email);
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		User savedUser = userRepository.save(user1);
		//	user.setId(savedUser.getUserId());
		//	System.out.println(savedUser.getPassword());
		MyUserDetails myUserDetails = MyUserDetails.build(savedUser);
		String jwtToken = jwtService.generateToken(myUserDetails);
		AuthenticationResponse authenticationResponse = new AuthenticationResponse();
		authenticationResponse.setToken(jwtToken);
		return authenticationResponse;
		//	return user;
	}
  

	@Override
	public UserUpdateDto findUserByEmail(String email) throws BattleException {
		Optional<User> optionalUser = userRepository.findByEmail(email);
		if (!optionalUser.isPresent()) {
			throw new BattleException("User id Not Found!");
		}
		User user = optionalUser.get();
		UserUpdateDto userUpdateDto = userToUpdatesUserDto(user);
		return userUpdateDto;
	}

	@Override
	public List<UserUpdateDto> getUsers() throws BattleException {
		List<User> users = userRepository.findAll();
		List<UserUpdateDto> userUpdateDto = new ArrayList<>();
		for (User u : users) {
			UserUpdateDto userDtos = userToUpdatesUserDto(u);
			userUpdateDto.add(userDtos);
		}
		return userUpdateDto;

	}


	@Override
	public UserUpdateDto getUserById(Long id) throws BattleException {
		Optional<User> optionalUser = userRepository.findById(id);
		if (!optionalUser.isPresent()) {
			throw new BattleException("User id Not Found!");
		}
		User user = optionalUser.get();
		UserUpdateDto userUpdateDto = userToUpdatesUserDto(user);
		return userUpdateDto;
	}


	@Override
	@Transactional
	public void deleteUserById(Long userId) throws BattleException {
		// Retrieve the user entity to delete
		Optional<User> optionalUser = userRepository.findById(userId);
		if (!optionalUser.isPresent()) {
			throw new BattleException("User id Not Found!");
		}
		User user = optionalUser.get();
		// Remove the user from all teams
		List<TeamMember> allTeamMembers = teamMemberRepository.findByUser(user);

		for (TeamMember teamMember : allTeamMembers) {
			//       teamMemberRepository.deleteByUser(teamMember);
			if (teamMember.getTeam().getCaptain().equals(teamMember.getUser())) {
				throw new BattleException("First Give captiancy to other Member!");
			}
			teamMemberService.deleteTeamMember(teamMember.getTeamMemberId(), userId);
		}

		for (Role r : user.getRoles()) {
			if (r.getName().equals(ERole.ROLE_ADMIN)) {
				throw new BattleException("ADMIN CAN'T DELETED!");
			}
			System.out.println(r.getName());
		}
		user.setRoles(null);
		// Delete the user entity
		userRepository.delete(user);
	}

	public UserDto userToUserDto(User user) {
		UserDto userDto = new UserDto();
		userDto.setId(user.getUserId());
		userDto.setName(user.getName());
		userDto.setEmail(user.getEmail());
		userDto.setPassword(user.getPassword());
		return userDto;
	}

	public User userDtoToUser(UserDto userDto) {
		User user = new User();
		user.setUserId(userDto.getId());
		user.setName(userDto.getName());
		user.setEmail(userDto.getEmail());
		user.setPassword(userDto.getPassword());
		return user;
	}


	public UserUpdateDto updateUser(UserUpdateDto userDto, Long userId) throws BattleException {
		Optional<User> optionalUser = userRepository.findById(userId);
		if (!optionalUser.isPresent()) {
			throw new BattleException("User id Not Found!");
		}

		User user = optionalUser.get();
		user.setEmail(userDto.getEmail());
		user.setName(userDto.getName());
		user.setUserImageName(userDto.getUserImageName());
		user.setBgmiId(userDto.getBgmiId());
		UserUpdateDto userDtos = userToUpdatesUserDto(user);
		return userDtos;
	}

	public UserUpdateDto userToUpdatesUserDto(User user) throws BattleException {


		UserUpdateDto updatedUser = new UserUpdateDto();
		updatedUser.setEmail(user.getEmail());
		updatedUser.setName(user.getName());
		updatedUser.setUserId(user.getUserId());
		updatedUser.setUserImageName(user.getUserImageName());
		updatedUser.setBgmiId(user.getBgmiId());
		return updatedUser;
	}


}


