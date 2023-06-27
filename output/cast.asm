        Jump         $$main                    
        DLabel       $eat-location-zero        
        DataZ        8                         
        DLabel       $print-format-integer     
        DataC        37                        %% "%d"
        DataC        100                       
        DataC        0                         
        DLabel       $print-format-floating    
        DataC        37                        %% "%f"
        DataC        102                       
        DataC        0                         
        DLabel       $print-format-boolean     
        DataC        37                        %% "%s"
        DataC        115                       
        DataC        0                         
        DLabel       $print-format-newline     
        DataC        10                        %% "\n"
        DataC        0                         
        DLabel       $print-format-string      
        DataC        37                        %% "%s"
        DataC        115                       
        DataC        0                         
        DLabel       $print-format-character   
        DataC        37                        %% "%c"
        DataC        99                        
        DataC        0                         
        DLabel       $print-format-space       
        DataC        32                        %% " "
        DataC        0                         
        DLabel       $print-format-tabspace    
        DataC        9                         %% "\t"
        DataC        0                         
        DLabel       $boolean-true-string      
        DataC        116                       %% "true"
        DataC        114                       
        DataC        117                       
        DataC        101                       
        DataC        0                         
        DLabel       $boolean-false-string     
        DataC        102                       %% "false"
        DataC        97                        
        DataC        108                       
        DataC        115                       
        DataC        101                       
        DataC        0                         
        DLabel       $errors-general-message   
        DataC        82                        %% "Runtime error: %s\n"
        DataC        117                       
        DataC        110                       
        DataC        116                       
        DataC        105                       
        DataC        109                       
        DataC        101                       
        DataC        32                        
        DataC        101                       
        DataC        114                       
        DataC        114                       
        DataC        111                       
        DataC        114                       
        DataC        58                        
        DataC        32                        
        DataC        37                        
        DataC        115                       
        DataC        10                        
        DataC        0                         
        Label        $$general-runtime-error   
        PushD        $errors-general-message   
        Printf                                 
        Halt                                   
        DLabel       $errors-int-divide-by-zero 
        DataC        105                       %% "integer divide by zero"
        DataC        110                       
        DataC        116                       
        DataC        101                       
        DataC        103                       
        DataC        101                       
        DataC        114                       
        DataC        32                        
        DataC        100                       
        DataC        105                       
        DataC        118                       
        DataC        105                       
        DataC        100                       
        DataC        101                       
        DataC        32                        
        DataC        98                        
        DataC        121                       
        DataC        32                        
        DataC        122                       
        DataC        101                       
        DataC        114                       
        DataC        111                       
        DataC        0                         
        Label        $$i-divide-by-zero        
        PushD        $errors-int-divide-by-zero 
        Jump         $$general-runtime-error   
        DLabel       $errors-float-divide-by-zero 
        DataC        102                       %% "floating divide by zero"
        DataC        108                       
        DataC        111                       
        DataC        97                        
        DataC        116                       
        DataC        105                       
        DataC        110                       
        DataC        103                       
        DataC        32                        
        DataC        100                       
        DataC        105                       
        DataC        118                       
        DataC        105                       
        DataC        100                       
        DataC        101                       
        DataC        32                        
        DataC        98                        
        DataC        121                       
        DataC        32                        
        DataC        122                       
        DataC        101                       
        DataC        114                       
        DataC        111                       
        DataC        0                         
        Label        $$f-divide-by-zero        
        PushD        $errors-float-divide-by-zero 
        Jump         $$general-runtime-error   
        DLabel       $usable-memory-start      
        DLabel       $global-memory-block      
        DataZ        21                        
        Label        $$main                    
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% test
        PushI        99                        
        Nop                                    
        StoreI                                 
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% test
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        4                         
        Add                                    %% test2
        PushI        120                       
        PushI        127                       
        BTAnd                                  
        StoreC                                 
        PushD        $global-memory-block      
        PushI        4                         
        Add                                    %% test2
        LoadC                                  
        PushD        $print-format-character   
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        5                         
        Add                                    %% test6
        PushI        777                       
        PushI        127                       
        BTAnd                                  
        StoreC                                 
        PushD        $global-memory-block      
        PushI        5                         
        Add                                    %% test6
        LoadC                                  
        PushD        $print-format-character   
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        6                         
        Add                                    %% test7
        PushI        99                        
        JumpTrue     -char-to-boolean-cast-1-true 
        Jump         -char-to-boolean-cast-1-false 
        Label        -char-to-boolean-cast-1-true 
        PushI        1                         
        Jump         -char-to-boolean-cast-1-join 
        Label        -char-to-boolean-cast-1-false 
        PushI        0                         
        Jump         -char-to-boolean-cast-1-join 
        Label        -char-to-boolean-cast-1-join 
        StoreC                                 
        PushD        $global-memory-block      
        PushI        6                         
        Add                                    %% test7
        LoadC                                  
        JumpTrue     -print-boolean-2-true     
        PushD        $boolean-false-string     
        Jump         -print-boolean-2-join     
        Label        -print-boolean-2-true     
        PushD        $boolean-true-string      
        Label        -print-boolean-2-join     
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        7                         
        Add                                    %% test8
        PushI        4                         
        JumpTrue     -int-to-boolean-cast-3-true 
        Jump         -int-to-boolean-cast-3-false 
        Label        -int-to-boolean-cast-3-true 
        PushI        1                         
        Jump         -int-to-boolean-cast-3-join 
        Label        -int-to-boolean-cast-3-false 
        PushI        0                         
        Jump         -int-to-boolean-cast-3-join 
        Label        -int-to-boolean-cast-3-join 
        StoreC                                 
        PushD        $global-memory-block      
        PushI        7                         
        Add                                    %% test8
        LoadC                                  
        JumpTrue     -print-boolean-4-true     
        PushD        $boolean-false-string     
        Jump         -print-boolean-4-join     
        Label        -print-boolean-4-true     
        PushD        $boolean-true-string      
        Label        -print-boolean-4-join     
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        8                         
        Add                                    %% test4
        PushI        4                         
        ConvertF                               
        StoreF                                 
        PushD        $global-memory-block      
        PushI        8                         
        Add                                    %% test4
        LoadF                                  
        PushD        $print-format-floating    
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        16                        
        Add                                    %% test5
        PushF        3.330000                  
        ConvertI                               
        StoreI                                 
        PushD        $global-memory-block      
        PushI        16                        
        Add                                    %% test5
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        20                        
        Add                                    %% test9
        PushI        0                         
        JumpTrue     -int-to-boolean-cast-5-true 
        Jump         -int-to-boolean-cast-5-false 
        Label        -int-to-boolean-cast-5-true 
        PushI        1                         
        Jump         -int-to-boolean-cast-5-join 
        Label        -int-to-boolean-cast-5-false 
        PushI        0                         
        Jump         -int-to-boolean-cast-5-join 
        Label        -int-to-boolean-cast-5-join 
        StoreC                                 
        PushD        $global-memory-block      
        PushI        20                        
        Add                                    %% test9
        LoadC                                  
        JumpTrue     -print-boolean-6-true     
        PushD        $boolean-false-string     
        Jump         -print-boolean-6-join     
        Label        -print-boolean-6-true     
        PushD        $boolean-true-string      
        Label        -print-boolean-6-join     
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        Halt                                   
