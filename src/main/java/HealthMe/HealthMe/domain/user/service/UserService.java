package HealthMe.HealthMe.domain.user.service;

import HealthMe.HealthMe.common.exception.CustomException;
import HealthMe.HealthMe.common.exception.ErrorCode;
import HealthMe.HealthMe.common.token.dto.AuthToken;
import HealthMe.HealthMe.common.token.AuthTokenProvider;
import HealthMe.HealthMe.domain.user.domain.User;
import HealthMe.HealthMe.domain.user.dto.*;
import HealthMe.HealthMe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final AuthTokenProvider authTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Transactional
    public UserDto signUp(UserSignUpDto userSignUpDto) throws CustomException{
        if(userSignUpDto.getEmail() == null){
            throw new CustomException(ErrorCode.EMAIL_NOT_FOUND);
        }
        if(userSignUpDto.getPassword() ==null){
            throw new CustomException(ErrorCode.PASSWORD_NOT_FOUND);
        }

        if(userRepository.findByEmail(userSignUpDto.getEmail()).isPresent()){
            throw new CustomException(ErrorCode.EMAIL_EXSIST);
        }

        User newUser = userSignUpDto.toEntity();
        newUser.hashPassword(bCryptPasswordEncoder);
        User save = userRepository.save(newUser);
        UserDto savedDto = UserDto.builder()
                .name(save.getName())
                .email(save.getEmail())
                .build();

        return savedDto;
    }
    @Transactional
    public UserDto insertBodyInformation(UserDto userInformationDto) throws CustomException{
        if(userInformationDto==null){
            throw new CustomException(ErrorCode.OBJECT_NOT_FOUND);
        }
        String email = userInformationDto.getEmail();
        if(email == null){
            throw new CustomException(ErrorCode.EMAIL_NOT_FOUND);
        }

        User user = userRepository.findByEmail(userInformationDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        user.setUserBodyInformation(userInformationDto.getName(),
                userInformationDto.getBirthday(),
                userInformationDto.getHeight(),
                userInformationDto.getWeight(),
                userInformationDto.getGender());

        return userInformationDto;
    }


    public UserDto getBodyInformation(UserDto userDto) throws CustomException {
        if(userDto == null){
            throw new CustomException(ErrorCode.OBJECT_NOT_FOUND);
        }
        if(userDto.getEmail() == null){
            throw new CustomException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        User user = userRepository.findByEmail(userDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        return UserDto.builder()
                .height(user.getHeight())
                .weight(user.getWeight())
                .birthday(user.getBirthday())
                .gender(user.getGender())
                .build();
    }


    @Transactional
    public UserDto changePassword(UserPasswordChangeDto userPasswordChangeDto) throws CustomException{
        if(userPasswordChangeDto == null){
            throw new CustomException(ErrorCode.OBJECT_NOT_FOUND);
        }

        String email = userPasswordChangeDto.getEmail();
        if(email==null){
            throw new CustomException(ErrorCode.EMAIL_NOT_FOUND);
        }

        User findUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        findUser.updatePassword(userPasswordChangeDto.getChangedPassword());
        findUser.hashPassword(bCryptPasswordEncoder);

        return UserDto.builder()
                .name(findUser.getName())
                .email(findUser.getPassword())
                .build();
    }

    public boolean checkPassword(UserPasswordChangeDto userPasswordChangeDto) throws CustomException{
        if(userPasswordChangeDto == null){
            throw new CustomException(ErrorCode.OBJECT_NOT_FOUND);
        }

        String email = userPasswordChangeDto.getEmail();
        if(email == null){
            throw new CustomException(ErrorCode.EMAIL_NOT_FOUND);
        }

        User findUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        if(!findUser.checkPassword(userPasswordChangeDto.getPassword(), bCryptPasswordEncoder)){
            return false;
        }

        return true;
    }

    private void updateRefreshToken(String email, String refreshToken) throws CustomException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);
    }

    public AuthToken signIn(LoginDto loginDto) throws CustomException {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        AuthToken authToken = authTokenProvider.generateToken(authentication);
        this.updateRefreshToken(loginDto.getEmail(), authToken.getRefreshToken());
        return authToken;
    }

    public boolean logout(LoginDto loginDto) throws CustomException {
        String email = loginDto.getEmail();
        if(email == null){
            throw new CustomException(ErrorCode.EMAIL_NOT_FOUND);
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        user.updateRefreshToken(null);
        userRepository.save(user);
        return true;
    }

}