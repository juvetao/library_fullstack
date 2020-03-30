package com.example.library_fullstack.controller;

import com.example.library_fullstack.data.AppUserRepository;
import com.example.library_fullstack.data.LibraryBookRepository;
import com.example.library_fullstack.dto.CreateAppUserForm;
import com.example.library_fullstack.dto.CreateLibraryBookForm;
import com.example.library_fullstack.entity.AppUser;
import com.example.library_fullstack.entity.LibraryBook;
import com.example.library_fullstack.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.lang.reflect.Field;
import java.time.LocalDate;

@Controller
public class AdminController {

    private AppUserRepository appUserRepository;
    private LibraryBookRepository libraryBookRepository;
    private AppUserService appUserService;

    @Autowired
    public AdminController(AppUserRepository appUserRepository, LibraryBookRepository libraryBookRepository, AppUserService appUserService) {
        this.appUserRepository = appUserRepository;
        this.libraryBookRepository = libraryBookRepository;
        this.appUserService = appUserService;
    }

    @GetMapping("/create/user")
    public String getCreateUserForm(Model model){
        model.addAttribute("form",new CreateAppUserForm());
        return "create-user";
    }

    @PostMapping("/create/user/process")
    public String postCreateUserForm(@Valid @ModelAttribute("form") CreateAppUserForm form, BindingResult bindingResult){

        if(appUserRepository.findByEmailIgnoreCase(form.getEmail()).isPresent()){
            FieldError error = new FieldError("form", "email", "Email is already in use");
            bindingResult.addError(error);
        }

        if(!form.getPassword().equals(form.getPasswordConfirm())){
            FieldError error = new FieldError("form", "passwordConfirm", "Your confirmation didn't match password");
            bindingResult.addError(error);
        }

        if(bindingResult.hasErrors()){
            return "create-user";
        }

        AppUser newAppUser = appUserService.registerAppUser(form.getFirstName(),form.getLastName(),form.getEmail(),form.getPassword(), LocalDate.now(),form.isAdmin());
        

        return "redirect:/index";
    }

    @GetMapping("/create/book")
    public String getCreateBookForm(Model model){
        model.addAttribute("form", new CreateLibraryBookForm());
        return "create-book";
    }

    @PostMapping("/create/book/process")
    public String processCreateBookForm(@Valid @ModelAttribute("form") CreateLibraryBookForm form, BindingResult bindingResult){


        if(form.getMaxLoanDays().length() < 1){
            FieldError fieldError = new FieldError("form","maxLoanDays","Enter max amount of loan days");
            bindingResult.addError(fieldError);
        }

        if (bindingResult.hasErrors()){
            return "create-book";
        }

        LibraryBook libraryBook = new LibraryBook(form.getTitle(),Integer.valueOf(form.getMaxLoanDays()));
        libraryBookRepository.save(libraryBook);
        return "redirect:/books";
    }

}
