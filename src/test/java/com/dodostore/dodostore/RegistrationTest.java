package com.dodostore.dodostore;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.dodostore.dodostore.controller.web.AuthenticationController;
import com.dodostore.dodostore.dto.request.CreateUserRequest;
import com.dodostore.dodostore.service.UserService;
import com.dodostore.dodostore.service.impl.UserServiceImpl;
import com.dodostore.dodostore.repository.UserRepository;
import com.dodostore.dodostore.mapper.UserMapper;
import com.dodostore.dodostore.entity.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class RegistrationTest {
 @Test void requiresNameAndEmailOnPublicRegistration() throws Exception {
  UserService service=mock(UserService.class);AuthenticationController controller=new AuthenticationController();ReflectionTestUtils.setField(controller,"userService",service);
  var mvc=MockMvcBuilders.standaloneSetup(controller).build();
  mvc.perform(post("/register").param("username","newuser").param("password","abcdefgh").param("fullName"," ").param("email",""))
    .andExpect(status().isOk()).andExpect(view().name("web/register")).andExpect(model().attributeHasFieldErrors("newUser","fullName","email"));
  verifyNoInteractions(service);
 }
 @Test void rejectsInvalidEmailAndAcceptsGmail() throws Exception {
  UserService service=mock(UserService.class);AuthenticationController controller=new AuthenticationController();ReflectionTestUtils.setField(controller,"userService",service);
  var mvc=MockMvcBuilders.standaloneSetup(controller).build();
  mvc.perform(post("/register").param("username","newuser").param("password","abcdefgh").param("fullName","Nguyễn Đức Anh").param("email","invalid"))
    .andExpect(view().name("web/register")).andExpect(model().attributeHasFieldErrors("newUser","email"));
  verifyNoInteractions(service);
  mvc.perform(post("/register").param("username","newuser").param("password","abcdefgh").param("fullName","Nguyễn Đức Anh").param("email","ducanh@gmail.com"))
    .andExpect(view().name("/web/login")).andExpect(model().hasNoErrors());
  ArgumentCaptor<CreateUserRequest> request=ArgumentCaptor.forClass(CreateUserRequest.class);verify(service).registerUser(request.capture());
  assertEquals("Nguyễn Đức Anh",request.getValue().getFullName());assertEquals("ducanh@gmail.com",request.getValue().getEmail());
 }
 @Test void savesNameAndEmailWithEncodedPassword() {
  UserRepository repo=mock(UserRepository.class);PasswordEncoder encoder=mock(PasswordEncoder.class);when(encoder.encode("abcdefgh")).thenReturn("hashed");
  UserServiceImpl service=new UserServiceImpl();ReflectionTestUtils.setField(service,"userRepository",repo);ReflectionTestUtils.setField(service,"passwordEncoder",encoder);
  ReflectionTestUtils.setField(service,"userMapper",org.mapstruct.factory.Mappers.getMapper(UserMapper.class));
  service.registerUser(CreateUserRequest.builder().username("newuser").password("abcdefgh").fullName(" Nguyễn Đức Anh ").email("DucAnh@gmail.com").build());
  ArgumentCaptor<UserEntity> saved=ArgumentCaptor.forClass(UserEntity.class);verify(repo).save(saved.capture());
  assertEquals("Nguyễn Đức Anh",saved.getValue().getFullName());assertEquals("ducanh@gmail.com",saved.getValue().getEmail());assertEquals("hashed",saved.getValue().getPassword());
 }
}
