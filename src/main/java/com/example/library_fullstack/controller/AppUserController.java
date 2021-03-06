package com.example.library_fullstack.controller;

import com.example.library_fullstack.data.AppUserRepository;
import com.example.library_fullstack.data.LibraryBookRepository;
import com.example.library_fullstack.data.LoanRepository;
import com.example.library_fullstack.dto.CreateLoanForm;
import com.example.library_fullstack.entity.AppUser;
import com.example.library_fullstack.entity.LibraryBook;
import com.example.library_fullstack.entity.Loan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

@Controller
public class AppUserController {


    private LibraryBookRepository libraryBookRepository;
    private AppUserRepository appUserRepository;
    private LoanRepository loanRepository;

    @Autowired
    public AppUserController(LibraryBookRepository libraryBookRepository, AppUserRepository appUserRepository, LoanRepository loanRepository) {
        this.libraryBookRepository = libraryBookRepository;
        this.appUserRepository = appUserRepository;
        this.loanRepository = loanRepository;
    }

    @GetMapping("/books")
    public String getBookView(Model model,@RequestParam(value = "search", defaultValue = "all") String search){
        System.out.println(search);
        List<LibraryBook> libraryBookList = new ArrayList<>();
//        Set the defaultValue as "all", so when at first time visiting this page without entering keyword, the whole list will be shown
        if (search.equals("all")){
            libraryBookList = libraryBookRepository.findAll();
        }else{ //When entering some keyword, it will be changed to the following method
            libraryBookList = libraryBookRepository.findByTitleContainsIgnoreCase(search);
            //When the list is empty, which means nothing matches the keyword or nothing found after entering keyword, a message will be shown
            if (libraryBookList.size() < 1){
                model.addAttribute("message","Your search '"+search+"' didn't match any book...");
            }
        }
        model.addAttribute("bookList",libraryBookList);
        return "books-view";
    }

    @GetMapping("/create/loan/{libraryBookId}")
    public String getCreateLoanForm(Model model, @PathVariable("libraryBookId") int libraryBookId){
        model.addAttribute("form",new CreateLoanForm());
        model.addAttribute("libraryBookId",libraryBookId);
        LibraryBook book = libraryBookRepository.findById(libraryBookId).orElseThrow(IllegalArgumentException::new);
        model.addAttribute("book",book);
        return "create-loan";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @PostMapping("/create/loan/process")
    public String processCreateLoanForm(@Valid @ModelAttribute("form") CreateLoanForm form, BindingResult bindingResult, Model model){

        if (!form.getEndDate().isEmpty() && LocalDate.parse(form.getEndDate()).isBefore(LocalDate.parse(form.getStartDate()))){
            FieldError error = new FieldError("form","endDate","Return date can't be before initial loan date");
            bindingResult.addError(error);
        }

        if (!form.getEndDate().isEmpty() && LocalDate.parse(form.getStartDate()).isBefore(LocalDate.now())){
            FieldError error = new FieldError("form","startDate","Enter a date after "+LocalDate.now().minusDays(1));
            bindingResult.addError(error);
        }

        if (!form.getEndDate().isEmpty() && DAYS.between(LocalDate.parse(form.getStartDate()),LocalDate.parse(form.getEndDate()))
            > libraryBookRepository.findById(Integer.valueOf(form.getLibraryBookId())).orElseThrow(IllegalArgumentException::new).getMaxLoanDays()
            && LocalDate.parse(form.getStartDate()).isBefore(LocalDate.parse(form.getEndDate()))){
            FieldError error = new FieldError("form","endDate","Loan period exceeds max loan days for this book");
            bindingResult.addError(error);
        }

        if (bindingResult.hasErrors()){
            model.addAttribute("form",form);
            model.addAttribute("libraryBookId",Integer.valueOf(form.getLibraryBookId()));
            LibraryBook book = libraryBookRepository.findById(Integer.valueOf(form.getLibraryBookId())).orElseThrow(IllegalArgumentException::new);
            model.addAttribute("book",book);
            return "create-loan";
        }

        AppUser user = appUserRepository.findByEmailIgnoreCase(form.getAppUserEmail()).orElseThrow(IllegalArgumentException::new);
        LibraryBook book = libraryBookRepository.findById(Integer.valueOf(form.getLibraryBookId())).orElseThrow(IllegalAccessError::new);
        book.setAvailable(false);
        Loan loan = new Loan(LocalDate.parse(form.getStartDate()),LocalDate.parse(form.getEndDate()), user,book);
        user.addLoan(loan);
        loanRepository.save(loan);
        return "redirect:/index";
    }


}
