package io.jieum.gyulbackendrepository.domain.auth.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequestDto (

    @NotBlank(message = "이메일은 필수 항목입니다.")
    @Size(min = 5, max = 30, message = "이메일은 최소 5글자, 최대 30글자여야 합니다.")
    String email,

    @NotBlank(message = "pw는 필수 항목입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 최소 8글자, 최대 20글자여야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
    String password,

    @NotBlank(message = "회원 이름은 필수 항목입니다.")
    @Size(min = 1, max = 10, message = "이름은 최소 1글자, 최대 10글자 미만이어야 합니다.")
    String name,

    @NotBlank(message = "성별은 필수항목입니다.")
    @Size(min = 1, max = 6, message = "성별은 최소 1글자, 최대 6글자 이하로 입력해야합니다.")
    String gender
) {
}
