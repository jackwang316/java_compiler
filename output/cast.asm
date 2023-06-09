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
        DLabel       $usable-memory-start      
        DLabel       $global-memory-block      
        DataZ        4                         
        Label        $$main                    
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% test
        PushI        0                         
        JumpTrue     -int-to-boolean-cast-1-true 
        Jump         -int-to-boolean-cast-1-false 
        Label        -int-to-boolean-cast-1-true 
        PushI        1                         
        Jump         -int-to-boolean-cast-1-join 
        Label        -int-to-boolean-cast-1-false 
        PushI        0                         
        Jump         -int-to-boolean-cast-1-join 
        Label        -int-to-boolean-cast-1-join 
        StoreC                                 
        PushD        $global-memory-block      
        PushI        1                         
        Add                                    %% test2
        PushI        1                         
        JumpTrue     -int-to-boolean-cast-2-true 
        Jump         -int-to-boolean-cast-2-false 
        Label        -int-to-boolean-cast-2-true 
        PushI        1                         
        Jump         -int-to-boolean-cast-2-join 
        Label        -int-to-boolean-cast-2-false 
        PushI        0                         
        Jump         -int-to-boolean-cast-2-join 
        Label        -int-to-boolean-cast-2-join 
        StoreC                                 
        PushD        $global-memory-block      
        PushI        2                         
        Add                                    %% test3
        PushI        0                         
        JumpTrue     -char-to-boolean-cast-3-true 
        Jump         -char-to-boolean-cast-3-false 
        Label        -char-to-boolean-cast-3-true 
        PushI        1                         
        Jump         -char-to-boolean-cast-3-join 
        Label        -char-to-boolean-cast-3-false 
        PushI        0                         
        Jump         -char-to-boolean-cast-3-join 
        Label        -char-to-boolean-cast-3-join 
        StoreC                                 
        PushD        $global-memory-block      
        PushI        3                         
        Add                                    %% test4
        PushI        99                        
        JumpTrue     -char-to-boolean-cast-4-true 
        Jump         -char-to-boolean-cast-4-false 
        Label        -char-to-boolean-cast-4-true 
        PushI        1                         
        Jump         -char-to-boolean-cast-4-join 
        Label        -char-to-boolean-cast-4-false 
        PushI        0                         
        Jump         -char-to-boolean-cast-4-join 
        Label        -char-to-boolean-cast-4-join 
        StoreC                                 
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% test
        LoadC                                  
        JumpTrue     -print-boolean-5-true     
        PushD        $boolean-false-string     
        Jump         -print-boolean-5-join     
        Label        -print-boolean-5-true     
        PushD        $boolean-true-string      
        Label        -print-boolean-5-join     
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        1                         
        Add                                    %% test2
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
        PushD        $global-memory-block      
        PushI        2                         
        Add                                    %% test3
        LoadC                                  
        JumpTrue     -print-boolean-7-true     
        PushD        $boolean-false-string     
        Jump         -print-boolean-7-join     
        Label        -print-boolean-7-true     
        PushD        $boolean-true-string      
        Label        -print-boolean-7-join     
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        3                         
        Add                                    %% test4
        LoadC                                  
        JumpTrue     -print-boolean-8-true     
        PushD        $boolean-false-string     
        Jump         -print-boolean-8-join     
        Label        -print-boolean-8-true     
        PushD        $boolean-true-string      
        Label        -print-boolean-8-join     
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        Halt                                   
